package mchorse.aperture.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mchorse.aperture.Aperture;
import mchorse.aperture.utils.CodelineParser;
import net.optifine.shaders.IShaderPack;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.config.ShaderOption;
import net.optifine.shaders.config.ShaderOptionVariable;
import net.optifine.shaders.config.ShaderOptionVariableConst;
import net.optifine.shaders.config.ShaderPackParser;
import net.optifine.shaders.uniform.ShaderUniform1f;
import net.optifine.shaders.uniform.ShaderUniform1i;

public class AsmShaderHandler
{
    public static final String owner = "mchorse/aperture/client/AsmShaderHandler";
    public static final String uniformPrefix = "_uniform_";

    public static final Pattern PATTERN_DEFINE = Pattern.compile("^\\s*#define\\s", Pattern.CASE_INSENSITIVE);
    public static final Pattern PATTERN_IF = Pattern.compile("^\\s*#(?:el)?(?:if)\\s", Pattern.CASE_INSENSITIVE);
    public static final Pattern PATTERN_CONST = Pattern.compile("^\\s*const\\s");
    public static final Pattern PATTERN_CASE = Pattern.compile("^\\s*case\\s");
    public static final Pattern PATTERN_ARRAY = Pattern.compile("\\[\\s*[_A-Za-z].*\\s*\\]");

    public static final Map<String, Integer> uniform1i = new HashMap<String, Integer>();
    public static final Map<String, Float> uniform1f = new HashMap<String, Float>();

    public static final Map<String, ShaderUniform1i> option1i = new LinkedHashMap<>();
    public static final Map<String, ShaderUniform1f> option1f = new LinkedHashMap<>();

    public static final Map<String, String> cachedShaders = new HashMap<>();
    public static final Map<String, List<String>> cachedIncludes = new HashMap<>();

    public static CodelineParser caseParser = new CodelineParser(':');
    public static CodelineParser constParser = new CodelineParser(';');

    public static float sunPathRotation;

    /**
     * ASM hook which is used in {@link mchorse.aperture.core.transformers.ShadersTransformer}<br>
     * Called by {@link net.optifine.shaders.Shaders#setProgramUniform1i(ShaderUniform1i, int)}
     */
    public static void setProgramUniform1i(ShaderUniform1i su, int value)
    {
        if (uniform1i.get(su.getName()) != null)
        {
            value = uniform1i.get(su.getName());
        }

        su.setValue(value);
    }

    /**
     * ASM hook which is used in {@link mchorse.aperture.core.transformers.ShadersTransformer}<br>
     * Called by {@link net.optifine.shaders.Shaders#setProgramUniform1f(ShaderUniform1f, int)}
     */
    public static void setProgramUniform1f(ShaderUniform1f su, float value)
    {
        if (uniform1f.get(su.getName()) != null)
        {
            value = uniform1f.get(su.getName());
        }
        else if ("sunPathRotation".equals(su.getName()) && sunPathRotation != value)
        {
            Shaders.sunPathRotation = sunPathRotation = value;
        }

        su.setValue(value);
    }

    /**
     * ASM hook which is used in {@link mchorse.aperture.core.transformers.ShadersTransformer}<br>
     * Called by {@link net.optifine.shaders.Shaders#useProgram(net.optifine.shaders.Program)}
     */
    public static void updateOptionUniforms()
    {
        for (Map.Entry<String, ShaderUniform1i> entry : option1i.entrySet())
        {
            entry.getValue().setProgram(Shaders.activeProgramID);
            setProgramUniform1i(entry.getValue(), Integer.parseInt(Shaders.getShaderOption(entry.getKey()).getValue()));
        }

        for (Map.Entry<String, ShaderUniform1f> entry : option1f.entrySet())
        {
            entry.getValue().setProgram(Shaders.activeProgramID);
            setProgramUniform1f(entry.getValue(), Float.parseFloat(Shaders.getShaderOption(entry.getKey()).getValue()));
        }
    }

    /**
     * ASM hook which is used in {@link mchorse.aperture.core.transformers.ShadersTransformer}<br>
     * Called by {@link net.optifine.shaders.Shaders#init()}
     */
    public static void afterInit()
    {
        for (ShaderUniform1i uniform : option1i.values())
        {
            uniform.reset();
        }

        for (ShaderUniform1f uniform : option1f.values())
        {
            uniform.reset();
        }

        sunPathRotation = Shaders.sunPathRotation;
    }

    /**
     * ASM hook which is used in {@link mchorse.aperture.core.transformers.ShadersTransformer}<br>
     * Called by {@link net.optifine.shaders.Shaders#loadShaderPack()}
     */
    public static void loadShaderPack()
    {
        cachedShaders.clear();
        cachedIncludes.clear();
        option1i.clear();
        option1f.clear();
    }

    /**
     * ASM hook which is used in {@link mchorse.aperture.core.transformers.ShaderPackParserTransformer}<br>
     * Called by {@link net.optifine.shaders.config.ShaderPackParser#collectShaderOptions(IShaderPack, String, Map)}
     */
    public static void collectShaderOptions()
    {
        caseParser.reset();
    }

    /**
     * ASM hook which is used in {@link mchorse.aperture.core.transformers.ShaderPackParserTransformer}<br>
     * Called by {@link net.optifine.shaders.config.ShaderPackParser#collectShaderOptions(IShaderPack, String, Map)}
     */
    public static ShaderOption getShaderOption(String line, String path, ShaderOption option, Map<String, ShaderOption> mapOptions)
    {
        if (option == null && (Aperture.optifineShaderOptionCurve == null || Aperture.optifineShaderOptionCurve.get()))
        {
            if (PATTERN_IF.matcher(line).find())
            {
                for (ShaderOption so : mapOptions.values())
                {
                    if (so instanceof ShaderUniformOption)
                    {
                        ShaderUniformOption uniform = (ShaderUniformOption) so;

                        if (uniform.isUniform())
                        {
                            uniform.checkMacro(line);
                        }
                    }
                }
            }
            else if (caseParser.cache.length() > 0 || PATTERN_CASE.matcher(line).find())
            {
                caseParser.parseLine(line);

                if (caseParser.isEnd)
                {
                    String caseLine = caseParser.cache.toString();

                    for (ShaderOption so : mapOptions.values())
                    {
                        if (so instanceof ShaderUniformOption)
                        {
                            ShaderUniformOption uniform = (ShaderUniformOption) so;

                            if (uniform.isUniform())
                            {
                                uniform.checkCase(caseLine);
                            }
                        }
                    }

                    caseParser.reset();
                }
            }
            else if (PATTERN_ARRAY.matcher(line).find())
            {
                for (ShaderOption so : mapOptions.values())
                {
                    if (so instanceof ShaderUniformOption)
                    {
                        ShaderUniformOption uniform = (ShaderUniformOption) so;

                        if (uniform.isUniform())
                        {
                            uniform.checkArray(line);
                        }
                    }
                }
            }
        }

        return option;
    }

    public static BufferedReader convert(Object obj) throws IOException
    {
        /*
         * It should be net.optifine.util.LineBuffer. This would be useful if Optifine has plans to release a G7 version for 1.12.2.
         */
        if (obj instanceof Iterable)
        {
            Iterator<String> iterator = ((Iterable<String>) obj).iterator();
            StringBuilder builder = new StringBuilder();

            while (iterator.hasNext())
            {
                builder.append(iterator.next()).append('\n');
            }

            return new BufferedReader(new StringReader(builder.toString()));
        }
        else
        {
            return (BufferedReader) obj;
        }
    }

    public static BufferedReader callResolveIncludes(Object reader, String filePath, IShaderPack shaderPack, int fileIndex, List<String> listFiles, int includeLevel) throws IOException
    {
        try
        {
            Method method = ShaderPackParser.class.getMethod("resolveIncludes", reader.getClass(), String.class, IShaderPack.class, int.class, List.class, int.class);

            return convert(method.invoke(null, reader, filePath, shaderPack, fileIndex, listFiles, includeLevel));
        }
        catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
        {
            Aperture.LOGGER.error("Do not support this version of Optifine!", e);

            return null;
        }
    }

    public static void addOptionUniform(ShaderUniformOption option)
    {
        if (option.isUniform())
        {
            switch (option.uniformType)
            {
                case ShaderUniformOption.INTEGER:

                    if (!option1i.containsKey(option.getName()))
                    {
                        option1i.put(option.getName(), new ShaderUniform1i(uniformPrefix + option.getName()));
                    }
                    break;

                case ShaderUniformOption.FLOAT:

                    if (!option1f.containsKey(option.getName()))
                    {
                        option1f.put(option.getName(), new ShaderUniform1f(uniformPrefix + option.getName()));
                    }
                    break;
            }
        }
    }

    public static void addOptionUniformConst(ShaderUniformConstOption option)
    {
        if (option.isUniform())
        {
            switch (option.type)
            {
                case "int":

                    if (!option1i.containsKey(option.getName()))
                    {
                        option1i.put(option.getName(), new ShaderUniform1i(option.getName()));
                    }
                    break;

                case "float":

                    if (!option1f.containsKey(option.getName()))
                    {
                        option1f.put(option.getName(), new ShaderUniform1f(option.getName()));
                    }
                    break;
            }
        }
    }

    public static String getConstLine(String line, BufferedReader reader, StringBuilder origin) throws IOException
    {
        origin.setLength(0);
        origin.append(line);

        constParser.reset();
        constParser.parseLine(line);

        while (!constParser.isEnd)
        {
            line = reader.readLine();

            if (line == null)
            {
                break;
            }

            constParser.parseLine(line);
            origin.append('\n').append(line);
        }

        return constParser.cache.toString();
    }

    /**
     * ASM hook which is used in {@link mchorse.aperture.core.transformers.ShadersTransformer}<br>
     * Called by {@link net.optifine.shaders.Shaders#createVertShader(net.optifine.shaders.Program, String)}<br>
     * Called by {@link net.optifine.shaders.Shaders#createGeomShader(net.optifine.shaders.Program, String)}<br>
     * Called by {@link net.optifine.shaders.Shaders#createFragShader(net.optifine.shaders.Program, String)}
     */
    public static BufferedReader getCachedShader(Object reader, String filePath, IShaderPack shaderPack, int fileIndex, List<String> listFiles, int includeLevel) throws IOException
    {
        if (!cachedShaders.containsKey(filePath))
        {
            BufferedReader resolved = callResolveIncludes(reader, filePath, shaderPack, fileIndex, listFiles, includeLevel);

            if (resolved == null)
            {
                return null;
            }

            if (!Aperture.optifineShaderOptionCurve.get())
            {
                return resolved;
            }

            cachedIncludes.put(filePath, new ArrayList<>(listFiles));

            StringBuilder builder = new StringBuilder();
            List<ShaderUniformOption> uniformOptions = new ArrayList<ShaderUniformOption>();
            List<ShaderUniformConstOption> uniformConstOptions = new ArrayList<ShaderUniformConstOption>();
            Set<String> uniforms = new HashSet<String>();
            Set<Pattern> definePatterns = new HashSet<Pattern>();
            Set<Pattern> constPatterns = new HashSet<Pattern>();

            for (ShaderOption option : Shaders.getShaderPackOptions())
            {
                if (option instanceof ShaderUniformOption)
                {
                    ShaderUniformOption uniform = (ShaderUniformOption) option;

                    if (uniform.isEnabled() && uniform.isUniform())
                    {
                        uniformOptions.add(uniform);
                        definePatterns.add(Pattern.compile(String.format("^\\s*#\\w+\\s+(\\S+)\\s+(?:.*\\W)?%s(?:\\W.*)?$", option.getName())));
                        constPatterns.add(Pattern.compile(String.format("^\\s*const\\s+\\S+\\s+(\\w+)\\s*=(?:.*\\W)?%s(?:\\W.*)?$", option.getName())));
                    }
                }
                else if (option instanceof ShaderUniformConstOption)
                {
                    ShaderUniformConstOption uniform = (ShaderUniformConstOption) option;

                    if (uniform.isEnabled() && uniform.isUniform())
                    {
                        uniformConstOptions.add(uniform);
                        definePatterns.add(Pattern.compile(String.format("^\\s*#\\w+\\s+(\\S+)\\s+(?:.*\\W)?%s(?:\\W.*)?$", option.getName())));
                        constPatterns.add(Pattern.compile(String.format("^\\s*const\\s+\\S+\\s+(\\w+)\\s*=(?:.*\\W)?%s(?:\\W.*)?$", option.getName())));
                    }
                }
            }

            ShaderUniformOption.doPatch = true;
            ShaderUniformConstOption.doPatch = true;
            String line;
            StringBuilder lineBuffer = new StringBuilder();

            while ((line = resolved.readLine()) != null)
            {
                boolean matched = false;

                if (PATTERN_DEFINE.matcher(line).find())
                {
                    for (ShaderUniformOption uniform : uniformOptions)
                    {
                        if (matched = uniform.matchesLine(line))
                        {
                            line = uniform.getSourceLine();

                            uniforms.add(String.format("uniform %s %s;\n", uniform.uniformType == ShaderUniformOption.INTEGER ? "int" : "float", uniformPrefix + uniform.getName()));
                            addOptionUniform(uniform);

                            break;
                        }
                    }

                    if (!matched)
                    {
                        String defVar = null;
                        for (Pattern pattern : definePatterns)
                        {
                            Matcher result = pattern.matcher(line);

                            if (result.find())
                            {
                                defVar = result.group(1);

                                definePatterns.add(Pattern.compile(String.format("^\\s*#\\w+\\s+(\\S+)\\s+(?:.*\\W)?%s(?:\\W.*)?$", defVar)));
                                constPatterns.add(Pattern.compile(String.format("^\\s*const\\s+\\w+\\s+(\\w+)\\s*=(?:.*\\W)?%s(?:\\W.*)?$", defVar)));

                                break;
                            }
                        }
                    }
                }

                if (!matched && PATTERN_CONST.matcher(line).find())
                {
                    line = getConstLine(line, resolved, lineBuffer);

                    for (ShaderUniformConstOption uniform : uniformConstOptions)
                    {
                        if (matched = uniform.matchesLine(line))
                        {
                            line = uniform.getSourceLine();

                            uniforms.add(String.format("uniform %s %s;\n/*\n%s\n*/\n", uniform.type, uniform.getName(), line));
                            addOptionUniformConst(uniform);

                            line = "";

                            break;
                        }
                    }

                    if (!matched)
                    {
                        String constVar = null;

                        for (Pattern pattern : constPatterns)
                        {
                            Matcher result = pattern.matcher(line);

                            if (result.find())
                            {
                                constVar = result.group(1);
                                line = lineBuffer.toString().replaceFirst("const\\s", "");

                                break;
                            }
                        }

                        if (constVar != null)
                        {
                            definePatterns.add(Pattern.compile(String.format("^\\s*#\\w+\\s+(\\S+)\\s+(?:.*\\W)?%s(?:\\W.*)?$", constVar)));
                            constPatterns.add(Pattern.compile(String.format("^\\s*const\\s+\\w+\\s+(\\w+)\\s*=(?:.*\\W)?%s(?:\\W.*)?$", constVar)));
                        }
                        else
                        {
                            line = lineBuffer.toString();
                        }
                    }
                }

                builder.append(line).append('\n');
            }

            resolved.close();
            ShaderUniformOption.doPatch = false;
            ShaderUniformConstOption.doPatch = false;

            int version = builder.indexOf("#version");
            int pos = builder.indexOf("#line", version);

            for (String uniform : uniforms)
            {
                builder.insert(pos, uniform);
            }

            cachedShaders.put(filePath, builder.toString());
        }

        if (reader instanceof BufferedReader)
        {
            ((BufferedReader) reader).close();
        }

        listFiles.clear();
        listFiles.addAll(cachedIncludes.get(filePath));

        return new BufferedReader(new StringReader(cachedShaders.get(filePath)));
    }

    /**
     * ASM hook which is used in {@link mchorse.aperture.core.transformers.ShaderOptionVariableTransformer}<br>
     * Called by {@link net.optifine.shaders.config.ShaderOptionVariable#parseOption(String, String)}
     */
    public static class ShaderUniformOption extends ShaderOptionVariable
    {
        public static final int NOT_SUPPORT = 0;
        public static final int INTEGER = 1;
        public static final int FLOAT = 2;

        public static boolean doPatch = false;

        public final Pattern defineChecker;
        public final Pattern caseChecker;
        public final Pattern arrayChecker;

        public int uniformType;

        public ShaderUniformOption(String name, String description, String value, String[] values, String path)
        {
            super(name, description, value, values, path);

            this.defineChecker = Pattern.compile(String.format(".*\\W%s(?:\\W.*)?", name));
            this.caseChecker = Pattern.compile(String.format("^\\s*case\\s+%s\\s*:", name));
            this.arrayChecker = Pattern.compile(String.format("\\[(?:.*\\W)?%s(?:\\W.*)?\\]", name));

            if (value != null && !this.checkReversedName(name))
            {
                boolean isInteger = true;
                boolean isFloat = true;

                for (String val : values)
                {
                    isInteger = isInteger && this.checkInt(val);
                    isFloat = isFloat && this.checkFloat(val);
                }

                uniformType = isInteger ? INTEGER : (isFloat ? FLOAT : NOT_SUPPORT);
            }
            else
            {
                uniformType = NOT_SUPPORT;
            }
        }

        @Override
        public boolean matchesLine(String line)
        {
            if (this.isUniform() && !doPatch)
            {
                return false;
            }

            return super.matchesLine(line);
        }

        @Override
        public String getSourceLine()
        {
            if (this.isUniform())
            {
                return "#define " + this.getName() + " " + uniformPrefix + this.getName();
            }

            return super.getSourceLine();
        }

        public boolean isUniform()
        {
            return this.uniformType != NOT_SUPPORT && (Aperture.optifineShaderOptionCurve == null || Aperture.optifineShaderOptionCurve.get());
        }

        public void checkMacro(String line)
        {
            if (this.defineChecker.matcher(line).matches())
            {
                this.uniformType = NOT_SUPPORT;
            }
        }

        public void checkCase(String line)
        {
            if (this.caseChecker.matcher(line).find())
            {
                this.uniformType = NOT_SUPPORT;
            }
        }

        public void checkArray(String line)
        {
            if (this.arrayChecker.matcher(line).find())
            {
                this.uniformType = NOT_SUPPORT;
            }
        }

        private boolean checkInt(String str)
        {
            try
            {
                Integer.parseInt(str);
                return true;
            }
            catch (NumberFormatException e)
            {
                return false;
            }
        }

        private boolean checkFloat(String str)
        {
            try
            {
                Float.parseFloat(str);
                return true;
            }
            catch (NumberFormatException e)
            {
                return false;
            }
        }

        private boolean checkReversedName(String name)
        {
            return name == null || name.contains("__") || name.toLowerCase().startsWith("gl_");
        }
    }

    /**
     * ASM hook which is used in {@link mchorse.aperture.core.transformers.ShaderOptionVariableConstTransformer}<br>
     * Called by {@link net.optifine.shaders.config.ShaderOptionVariableConst#parseOption(String, String)}
     */
    public static class ShaderUniformConstOption extends ShaderOptionVariableConst
    {
        public static boolean doPatch = false;

        public final boolean isUniform;
        public final String type;

        public ShaderUniformConstOption(String name, String type, String description, String value, String[] values, String path)
        {
            super(name, type, description, value, values, path);

            this.isUniform = "sunPathRotation".equals(name);
            this.type = type;
        }

        @Override
        public boolean matchesLine(String line)
        {
            if (this.isUniform() && !doPatch)
            {
                return false;
            }

            return super.matchesLine(line);
        }

        public boolean isUniform()
        {
            return this.isUniform && (Aperture.optifineShaderOptionCurve == null || Aperture.optifineShaderOptionCurve.get());
        }
    }
}
