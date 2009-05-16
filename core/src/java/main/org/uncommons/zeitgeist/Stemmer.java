package org.uncommons.zeitgeist;

/**
 * Stemmer, implementing the Porter Stemming Algorithm
 *
 * The Stemmer class transforms a word into its root form.  The input
 * word can be provided a character at time (by calling add()), or at once
 * by calling one of the various stem(something) methods.
 */
public class Stemmer
{
    private char[] buffer;
    private int i;     /* offset into buffer */
    private int i_end; /* offset to end of stemmed word */
    private int j;
    private int k;
    private static final int INC = 50;


    /* unit of size whereby buffer is increased */
    public Stemmer()
    {
        buffer = new char[INC];
        i = 0;
        i_end = 0;
    }


    /**
     * Adds wLen characters to the word being stemmed contained in a portion
     * of a char[] array. This is like repeated calls of add(char ch), but
     * faster.
     */
    private void add(String word)
    {
        char[] chars = word.toCharArray();
        if (i + chars.length >= buffer.length)
        {
            char[] newBuffer = new char[i + chars.length + INC];
            System.arraycopy(buffer, 0, newBuffer, 0, i);
            buffer = newBuffer;
        }
        for (char ch : chars)
        {
            buffer[i++] = ch;
        }
    }


    /**
     *  @return true if buffer[i] is a consonant.
     */
    private boolean isConsonant(int i)
    {
        switch (buffer[i])
        {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u': return false;
            case 'y': return (i == 0) || !isConsonant(i - 1);
            default: return true;
        }
    }


    /* measureConsonantSequences() measures the number of consonant sequences between 0 and j. if c is
       a consonant sequence and v a vowel sequence, and <..> indicates arbitrary
       presence,

          <c><v>       gives 0
          <c>vc<v>     gives 1
          <c>vcvc<v>   gives 2
          <c>vcvcvc<v> gives 3
          ....
    */
    private int measureConsonantSequences()
    {
        int n = 0;
        int i = 0;
        while (true)
        {
            if (i > j)
            {
                return n;
            }
            if (!isConsonant(i))
            {
                break;
            }
            i++;
        }
        i++;
        while (true)
        {
            while (true)
            {
                if (i > j)
                {
                    return n;
                }
                if (isConsonant(i))
                {
                    break;
                }
                i++;
            }
            i++;
            n++;
            while (true)
            {
                if (i > j)
                {
                    return n;
                }
                if (!isConsonant(i))
                {
                    break;
                }
                i++;
            }
            i++;
        }
    }


    /* vowelInStem() is true <=> 0,...j contains a vowel */
    private boolean vowelInStem()
    {
        int i;
        for (i = 0; i <= j; i++)
        {
            if (!isConsonant(i))
            {
                return true;
            }
        }
        return false;
    }


    /* doublec(j) is true <=> j,(j-1) contain a double consonant. */
    private boolean doublec(int j)
    {
        if (j < 1)
        {
            return false;
        }
        if (buffer[j] != buffer[j - 1])
        {
            return false;
        }
        return isConsonant(j);
    }


    /* cvc(i) is true <=> i-2,i-1,i has the form consonant - vowel - consonant
       and also if the second c is not w,x or y. this is used when trying to
       restore an e at the end of a short word. e.g.

          cav(e), lov(e), hop(e), crim(e), but
          snow, box, tray.

    */
    private boolean cvc(int i)
    {
        if (i < 2 || !isConsonant(i) || isConsonant(i - 1) || !isConsonant(i - 2))
        {
            return false;
        }
        int ch = buffer[i];
        return !(ch == 'w' || ch == 'x' || ch == 'y');
    }


    private boolean ends(String s)
    {
        int l = s.length();
        int o = k - l + 1;
        if (o < 0)
        {
            return false;
        }
        for (int i = 0; i < l; i++)
        {
            if (buffer[o + i] != s.charAt(i))
            {
                return false;
            }
        }
        j = k - l;
        return true;
    }


    /* setto(s) sets (j+1),...k to the characters in the string s, readjusting k. */
    private void setto(String s)
    {
        int l = s.length();
        int o = j + 1;
        for (int i = 0; i < l; i++)
        {
            buffer[o + i] = s.charAt(i);
        }
        k = j + l;
    }


    /* r(s) is used further down. */
    private void r(String s)
    {
        if (measureConsonantSequences() > 0)
        {
            setto(s);
        }
    }


    /* step1() gets rid of plurals and -ed or -ing. e.g.

           caresses  ->  caress
           ponies    ->  poni
           ties      ->  ti
           caress    ->  caress
           cats      ->  cat

           feed      ->  feed
           agreed    ->  agree
           disabled  ->  disable

           matting   ->  mat
           mating    ->  mate
           meeting   ->  meet
           milling   ->  mill
           messing   ->  mess

           meetings  ->  meet

    */

    private void step1()
    {
        if (buffer[k] == 's')
        {
            if (ends("sses"))
            {
                k -= 2;
            }
            else if (ends("ies"))
            {
                setto("i");
            }
            else if (buffer[k - 1] != 's')
            {
                k--;
            }
        }
        if (ends("eed"))
        {
            if (measureConsonantSequences() > 0)
            {
                k--;
            }
        }
        else if ((ends("ed") || ends("ing")) && vowelInStem())
        {
            k = j;
            if (ends("at"))
            {
                setto("ate");
            }
            else if (ends("bl"))
            {
                setto("ble");
            }
            else if (ends("iz"))
            {
                setto("ize");
            }
            else if (doublec(k))
            {
                k--;
                int ch = buffer[k];
                if (ch == 'l' || ch == 's' || ch == 'z')
                {
                    k++;
                }
            }
            else if (measureConsonantSequences() == 1 && cvc(k))
            {
                setto("e");
            }
        }
    }


    /* step2() turns terminal y to i when there is another vowel in the stem. */
    private void step2()
    {
        if (ends("y") && vowelInStem())
        {
            buffer[k] = 'i';
        }
    }

    /* step3() maps double suffices to single ones. so -ization ( = -ize plus
 -ation) maps to -ize etc. note that the string before the suffix must give
 measureConsonantSequences() > 0. */


    private final void step3()
    {
        if (k == 0)
        {
            return; /* For Bug 1 */
        }
        switch (buffer[k - 1])
        {
            case 'a':
                if (ends("ational"))
                {
                    r("ate");
                }
                else if (ends("tional"))
                {
                    r("tion");
                }
                break;
            case 'c':
                if (ends("enci"))
                {
                    r("ence");
                }
                else if (ends("anci"))
                {
                    r("ance");
                }
                break;
            case 'e':
                if (ends("izer"))
                {
                    r("ize");
                }
                break;
            case 'l':
                if (ends("bli"))
                {
                    r("ble");
                }
                else if (ends("alli"))
                {
                    r("al");
                }
                else if (ends("entli"))
                {
                    r("ent");
                }
                else if (ends("eli"))
                {
                    r("e");
                }
                else if (ends("ousli"))
                {
                    r("ous");
                }
                break;
            case 'o':
                if (ends("ization"))
                {
                    r("ize");
                }
                else if (ends("ation"))
                {
                    r("ate");
                }
                else if (ends("ator"))
                {
                    r("ate");
                }
                break;
            case 's':
                if (ends("alism"))
                {
                    r("al");
                }
                else if (ends("iveness"))
                {
                    r("ive");
                }
                else if (ends("fulness"))
                {
                    r("ful");
                }
                else if (ends("ousness"))
                {
                    r("ous");
                }
                break;
            case 't':
                if (ends("aliti"))
                {
                    r("al");
                }
                else if (ends("iviti"))
                {
                    r("ive");
                }
                else if (ends("biliti"))
                {
                    r("ble");
                }
                break;
            case 'g':
                if (ends("logi"))
                {
                    r("log");
                }
        }
    }


    /* step4() deals with -ic-, -full, -ness etc. similar strategy to step3. */
    private void step4()
    {
        switch (buffer[k])
        {
            case 'e':
                if (ends("icate"))
                {
                    r("ic");
                }
                else if (ends("ative"))
                {
                    r("");
                }
                else if (ends("alize"))
                {
                    r("al");
                }
                break;
            case 'i':
                if (ends("iciti"))
                {
                    r("ic");
                }
                break;
            case 'l':
                if (ends("ical"))
                {
                    r("ic");
                }
                else if (ends("ful"))
                {
                    r("");
                }
                break;
            case 's':
                if (ends("ness"))
                {
                    r("");
                }
        }
    }

    /* step5() takes off -ant, -ence etc., in context <c>vcvc<v>. */


    private void step5()
    {
        if (k == 0)
        {
            return; /* for Bug 1 */
        }
        switch (buffer[k - 1])
        {
            case 'a':
                if (ends("al"))
                {
                    break;
                }
                return;
            case 'c':
                if (ends("ance"))
                {
                    break;
                }
                if (ends("ence"))
                {
                    break;
                }
                return;
            case 'e':
                if (ends("er"))
                {
                    break;
                }
                return;
            case 'i':
                if (ends("ic"))
                {
                    break;
                }
                return;
            case 'l':
                if (ends("able"))
                {
                    break;
                }
                if (ends("ible"))
                {
                    break;
                }
                return;
            case 'n':
                if (ends("ant"))
                {
                    break;
                }
                if (ends("ement"))
                {
                    break;
                }
                if (ends("ment"))
                {
                    break;
                }
                /* element etc. not stripped before the measureConsonantSequences */
                if (ends("ent"))
                {
                    break;
                }
                return;
            case 'o':
                if (ends("ion") && j >= 0 && (buffer[j] == 's' || buffer[j] == 't'))
                {
                    break;
                }
                /* j >= 0 fixes Bug 2 */
                if (ends("ou"))
                {
                    break;
                }
                return;
            /* takes care of -ous */
            case 's':
                if (ends("ism"))
                {
                    break;
                }
                return;
            case 't':
                if (ends("ate"))
                {
                    break;
                }
                if (ends("iti"))
                {
                    break;
                }
                return;
            case 'u':
                if (ends("ous"))
                {
                    break;
                }
                return;
            case 'v':
                if (ends("ive"))
                {
                    break;
                }
                return;
            case 'z':
                if (ends("ize"))
                {
                    break;
                }
                return;
            default:
                return;
        }
        if (measureConsonantSequences() > 1)
        {
            k = j;
        }
    }

    /* step6() removes a final -e if measureConsonantSequences() > 1. */


    private void step6()
    {
        j = k;
        if (buffer[k] == 'e')
        {
            int a = measureConsonantSequences();
            if (a > 1 || a == 1 && !cvc(k - 1))
            {
                k--;
            }
        }
        if (buffer[k] == 'l' && doublec(k) && measureConsonantSequences() > 1)
        {
            k--;
        }
    }


    /**
     * Stem the word placed into the Stemmer buffer through calls to add().
     * Returns true if the stemming process resulted in a word different
     * from the input.  You can retrieve the result with
     * getResultLength()/getResultBuffer() or toString().
     */
    public String stem(String word)
    {
        add(word);
        k = i - 1;
        if (k > 1)
        {
            step1();
            step2();
            step3();
            step4();
            step5();
            step6();
        }
        i_end = k + 1;
        i = 0;
        return new String(buffer, 0, i_end);
    }
}