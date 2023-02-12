package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.ValidationObject;

// SUMMARY
// Static methods for validation

public class ValidationUtil {

    public final static Map<Integer, List<String>> WORDS = new HashMap<>() {
        {
            put(1, new ArrayList<>() {
                {

                }
            });
            put(2, new ArrayList<>() {
                {
                    add("an");
                    add("as");
                    add("at");
                    add("be");
                    add("by");
                    add("do");
                    add("he");
                    add("if");
                    add("in");
                    add("is");
                    add("it");
                    add("my");
                    add("no");
                    add("of");
                    add("on");
                    add("or");
                    add("so");
                    add("to");
                    add("up");
                    add("us");
                    add("we");
                }
            });
            put(3, new ArrayList<>() {
                {
                    add("act");
                    add("all");
                    add("and");
                    add("any");
                    add("are");
                    add("but");
                    add("can");
                    add("day");
                    add("did");
                    add("end");
                    add("far");
                    add("few");
                    add("for");
                    add("get");
                    add("god");
                    add("had");
                    add("has");
                    add("her");
                    add("him");
                    add("his");
                    add("how");
                    add("its");
                    add("law");
                    add("man");
                    add("may");
                    add("men");
                    add("not");
                    add("now");
                    add("off");
                    add("old");
                    add("one");
                    add("our");
                    add("own");
                    add("per");
                    add("say");
                    add("see");
                    add("set");
                    add("she");
                    add("the");
                    add("too");
                    add("two");
                    add("use");
                    add("war");
                    add("was");
                    add("way");
                    add("who");
                    add("why");
                    add("yet");
                    add("you");
                }
            });
            put(4, new ArrayList<>() {
                {
                    add("also");
                    add("baby");
                    add("back");
                    add("been");
                    add("both");
                    add("case");
                    add("does");
                    add("down");
                    add("each");
                    add("even");
                    add("from");
                    add("good");
                    add("have");
                    add("here");
                    add("into");
                    add("just");
                    add("life");
                    add("like");
                    add("long");
                    add("made");
                    add("make");
                    add("many");
                    add("more");
                    add("most");
                    add("much");
                    add("must");
                    add("only");
                    add("over");
                    add("part");
                    add("said");
                    add("same");
                    add("some");
                    add("such");
                    add("than");
                    add("that");
                    add("them");
                    add("then");
                    add("they");
                    add("this");
                    add("time");
                    add("upon");
                    add("used");
                    add("very");
                    add("were");
                    add("what");
                    add("when");
                    add("well");
                    add("will");
                    add("with");
                    add("work");
                    add("your");
                }
            });
            put(5, new ArrayList<>() {
                {
                    add("about");
                    add("above");
                    add("after");
                    add("again");
                    add("among");
                    add("being");
                    add("could");
                    add("early");
                    add("every");
                    add("first");
                    add("found");
                    add("given");
                    add("great");
                    add("group");
                    add("house");
                    add("human");
                    add("large");
                    add("later");
                    add("means");
                    add("might");
                    add("never");
                    add("often");
                    add("order");
                    add("other");
                    add("place");
                    add("point");
                    add("power");
                    add("right");
                    add("shall");
                    add("since");
                    add("small");
                    add("state");
                    add("still");
                    add("their");
                    add("there");
                    add("these");
                    add("think");
                    add("those");
                    add("three");
                    add("under");
                    add("until");
                    add("water");
                    add("where");
                    add("which");
                    add("while");
                    add("whole");
                    add("women");
                    add("world");
                    add("would");
                    add("years");
                }
            });
        }
    };

    public static boolean nullCheck(List<ValidationObject> vObjects, LogUtil log) {
        boolean result = false;
        for (ValidationObject obj : vObjects) {
            if (obj.object == null) {
                if (result == false) {
                    String message = String.format("%s provided was null", obj.cls.getSimpleName());
                    switch (obj.severity) {
                        case NONE:
                            log.i(message);
                            break;
                        case LOW:
                            log.w(message);
                            break;
                        case HIGH:
                            log.e(message);
                            result = true;
                            break;
                    }
                    result = true;
                }
            }
        }
        return result;
    }

    public static boolean namesMatch(String a, String b, String TAG) {
        LogUtil log = new LogUtil(TAG, "namesMatch");
        // Return if either are empty
        if (a.isEmpty() || b.isEmpty()) {
            return false;
        }

        // Return if they're equal
        if (a.equals(b)) {
            return true;
        }

        int count = 0;
        char[] str1 = a.toCharArray();
        char[] str2 = b.toCharArray();
        boolean str1Longer = a.length() > b.length();
        int length = (str1Longer) ? b.length() : a.length();

        for (int i = 0; i < length; i++) {
            try {
                if (str1[i] == str2[i]) {
                    count++;
                } else {
                    break;
                }
            } catch (Exception e) {
                log.e("still not working");
            }


        }

        // Return if there were no matches
        if (count == 0) {
            return false;
        } else if (count == length) {
            return true;
        }

        String strResult = a.substring(0, count);

        // Remove trailing spaces
        if (str1[count - 1] == ' ') {
            strResult = strResult.trim();
        }

        if (strResult.length() > 1 && strResult.length() <= 5) {
            if (WORDS.get(strResult.length()).contains(strResult.toLowerCase())) {
                return false;
            }
        }

        if (count < 3 && count > length - 3) {
            return true;
        }

        return false;
    }
}

