package mchorse.aperture.utils;

public class CodelineParser
{
    public final char lineEnd;

    public StringBuilder cache;
    public boolean isComment;
    public boolean isEnd;

    public CodelineParser(char lineEnd)
    {
        this.lineEnd = lineEnd;

        this.cache = new StringBuilder();
        this.isComment = false;
        this.isEnd = false;
    }

    public void reset()
    {
        this.cache.setLength(0);
        this.isComment = false;
        this.isEnd = false;
    }

    public void parseLine(String code)
    {
        code = code.trim();

        for (int i = 0; i < code.length() && !this.isEnd; i++)
        {
            char thisChar = code.charAt(i);
            char lastChar = 0;

            if (i > 0)
            {
                lastChar = code.charAt(i - 1);
            }

            if (this.isComment)
            {
                if (lastChar == '*' && thisChar == '/')
                {
                    this.isComment = false;
                }
            }
            else
            {
                if (lastChar == '/' && thisChar == '/')
                {
                    this.cache.setLength(this.cache.length() - 1);

                    break;
                }
                else if (lastChar == '/' && thisChar == '*')
                {
                    this.isComment = true;
                    this.cache.setLength(this.cache.length() - 1);
                }
                else if (thisChar == this.lineEnd)
                {
                    this.isEnd = true;
                }

                if (!this.isComment)
                {
                    this.cache.append(thisChar);
                }
            }
        }

        this.cache.append(' ');
    }
}
