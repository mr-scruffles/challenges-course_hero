import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * On our site, students can input the courses they are taking in any combination of a
 * Department+Course Number followed by Semester+Year. A department is always one or more
 * alphabetic characters, and a course number is always one or more numeric characters.
 * Semester is either an abbreviation or a word, and a Year is either two digits or four digits.
 * Semester and Year can be swapped in position. There is always a space between
 * Department+Course Number and Semester+Year.
 *
 * For example:
 *  CS111 2016 Fall
 *  CS-111 Fall 2016
 *  MATH 123 2015 Spring
 *
 * Examples of Department+Course Number:
 *  CS111
 *  CS 111
 *  CS:111
 *  CS-111
 *
 *  Examples of Semester+Year:
 *      Fall 2016
 *      fall 16
 *      2016 Fall
 *      F2016
 *      Fall2016
 *
 * Page 2
 * Semesters could be abbreviated as: F (Fall), W (Winter), S (Spring), Su (Summer)
 * Part 1: Data Normalization
 * Write code that, for each input format, identify the Department, Course Number, Year and
 * Semester. You should include input validation in your solution.
 * For example, all of the previous examples should yield the following output:
 *  Department: CS
 *  Course Number: 111
 *  Year: 2016
 *  Semester: Fall
 */
public class Main {

    public static void main(String[] args) {

        ArrayList<String> strings = new ArrayList<>();

        // Twos
        strings.add("CS111 F2016");
        strings.add("CS111 2016F");
        strings.add("CS111 2016W");
        strings.add("CS111 Su16");
        strings.add("CS:111 2016Fall");

        // Threes
        strings.add("CS:111 2016 Fall");
        strings.add("CS:111 2016S");
        strings.add("CS-111 S16");

        // Fours
        strings.add("CS 111 2016 S");
        strings.add("CS 111 W 16");

        CourseParser parse = new CourseParser();

        for (String string : strings) {
            parse.parseCourse(string);
            System.out.println("=================");
        }
    }
}

class CourseParser {

    private final HashMap<String, String> seasonMap;

    public CourseParser() {
        this.seasonMap = new HashMap<>();
        this.seasonMap.put("F", "Fall");
        this.seasonMap.put("S", "Spring");
        this.seasonMap.put("SU", "Summer");
        this.seasonMap.put("W", "Winter");
    }

    /**
     * ParseCourse will split the course string into tokens and parse the dept, sem, course num and year.
     * Depending on the length of the tokens the course will be in the form of:
     * 2 Token: [coursenum] [semyear]
     * 3 Token: [coursenum] [sem] [year]
     * 3 Token: [course] [num] [semyear]
     * 4 Token: [course] [num] [sem] [year]
     *
     * @param course The course string.
     */
    public void parseCourse(String course) {

        System.out.println(course);
        String[] tokens = course.split(" ");

        String dept = "";
        String courseNum = "";
        String year = "";
        String sem = "";

        List<String> semYear;
        List<String> deptCourse;

        switch (tokens.length) {
            case 2:
                deptCourse = extractDeptCourse(new String[]{tokens[0]});
                dept = deptCourse.get(0);
                courseNum = deptCourse.get(1);

                semYear = extractSemYear(new String[]{tokens[1]});
                sem = semYear.get(0);
                year = semYear.get(1);
                break;
            case 3:
                if(tokens[0].matches("[a-zA-Z]+.?[0-9]+")) {
                    deptCourse = extractDeptCourse(new String[]{tokens[0]});
                    dept = deptCourse.get(0);
                    courseNum = deptCourse.get(1);

                    semYear = extractSemYear(new String[]{tokens[1], tokens[2]});
                    sem = semYear.get(0);
                    year = semYear.get(1);
                } else {
                    deptCourse = extractDeptCourse(new String[]{tokens[0], tokens[1]});
                    dept = deptCourse.get(0);
                    courseNum = deptCourse.get(1);

                    semYear = extractSemYear(new String[]{tokens[2]});
                    sem = semYear.get(0);
                    year = semYear.get(1);
                }
                break;
            case 4:
                deptCourse = extractDeptCourse(new String[]{tokens[0], tokens[1]});
                dept = deptCourse.get(0);
                courseNum = deptCourse.get(1);

                semYear = extractSemYear(new String[]{tokens[2], tokens[3]});
                sem = semYear.get(0);
                year = semYear.get(1);
                break;
            default:
                break;
        }

        System.out.println("Department: " + dept);
        System.out.println("Course Number: " + courseNum);
        System.out.println("Year: " + year);
        System.out.println("Semester: " + sem);
    }

    /**
     * Extracts the department and course number.
     *
     * @param deptCourses Array of tokens.
     * @return Department and course number.
     */
    private List<String> extractDeptCourse(String[] deptCourses) {
        List<String> output = new ArrayList<>();

        // If there is one token then the form is [coursenum].
        // If there are two tokens then the form is [course num].
        if(deptCourses.length == 1) {
            String sanitized = deptCourses[0].replaceAll("[^a-zA-Z0-9]", "");
            List<String> deptCourse = parseToken(sanitized, "([a-zA-Z]+)(\\d+)");
            output.add(deptCourse.get(0));
            output.add(deptCourse.get(1));
        } else if(deptCourses.length == 2) {
            output.add(deptCourses[0]);
            output.add(deptCourses[1]);
        } else {
            output.add("");
            output.add("");
        }

        return output;
    }

    /**
     * Extracts semester and year from tokens;
     * @param semYears Array of tokens
     * @return A list of string values semester and year in that order.
     */
    private List<String> extractSemYear(String[] semYears) {

        List<String> output = new ArrayList<>();

        // If there is one token then the form is either [semyear] or [yearsem].
        // If there are two tokens then the form is either [sem year] or [year sem].
        if(semYears.length == 1) {
            if (Character.isLetter(semYears[0].charAt(0))) {
                List<String> result = parseToken(semYears[0], "([a-zA-Z]+)(\\d+)");
                output.add(buildSemesterHelper(result.get(0)));
                output.add(buildYearHelper(result.get(1)));
            } else {
                List<String> result = parseToken(semYears[0], "(\\d+)([a-zA-Z]+)");
                output.add(buildSemesterHelper(result.get(1)));
                output.add(buildYearHelper(result.get(0)));
            }
        } else if(semYears.length == 2) {
            if(Character.isLetter(semYears[0].charAt(0))) {
                output.add(buildSemesterHelper(semYears[0]));
                output.add(buildYearHelper(semYears[1]));
            } else {
                output.add(buildSemesterHelper(semYears[1]));
                output.add(buildYearHelper(semYears[0]));
            }
        } else {
            output.add("");
            output.add("");
        }
        return output;
    }

    /**
     * Builds the year. If the year is less then 4 characters then it appends the year 20.
     *
     * @param year The year string.
     * @return The year.
     */
    private String buildYearHelper(String year) {
        String yr;
        yr = (year.length() == 2) ? "20" + year : year;
        return yr;
    }

    /**
     * Builds the semester. If the semester is abbreviated then it returns the appropriate semester.
     *
     * @param semAbbr The semester abbreviation.
     * @return The semester.
     */
    private String buildSemesterHelper(String semAbbr) {
        return seasonMap.getOrDefault(semAbbr.toUpperCase(), semAbbr);
    }

    /**
     * This method sanitizes input token to remove all non alpha numeric
     * characters, then based on the regular expression param will parse
     * the token into 1 or many groups and returns the groups in the order
     * that are specified in the regex.
     *
     * @param token The input string to parse.
     * @param regex The regular expression.
     * @return A list of tokens in order of the regex expression.
     */
    private List<String> parseToken(String token, String regex) {

        List<String> output = new ArrayList<>();

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(token);
        m.matches();

        output.add(m.group(1));
        output.add(m.group(2));

        return output;
    }

    private void validateCourse(String course) {
        return;
    }
}
