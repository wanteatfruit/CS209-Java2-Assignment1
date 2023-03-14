import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * This is just a demo for you, please run it on JDK17.
 * This is just a demo, and you can extend and implement functions
 * based on this demo, or implement it in a different way.
 */
public class OnlineCoursesAnalyzer {

    List<Course> courses = new ArrayList<>();

    public OnlineCoursesAnalyzer(String datasetPath) {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
                Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
                        Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
                        Integer.parseInt(info[9]), Integer.parseInt(info[10]), Double.parseDouble(info[11]),
                        Double.parseDouble(info[12]), Double.parseDouble(info[13]), Double.parseDouble(info[14]),
                        Double.parseDouble(info[15]), Double.parseDouble(info[16]), Double.parseDouble(info[17]),
                        Double.parseDouble(info[18]), Double.parseDouble(info[19]), Double.parseDouble(info[20]),
                        Double.parseDouble(info[21]), Double.parseDouble(info[22]));
                courses.add(course);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //1
    public Map<String, Integer> getPtcpCountByInst() {
        Stream<Course> stream = courses.stream();
        Map<String,Integer> map = stream.sorted(Comparator.comparing(c -> c.institution))
                .collect(Collectors.groupingBy(c->c.institution,TreeMap::new,Collectors.summingInt(c->c.participants)));

        return map;
    }

    //2
    public Map<String, Integer> getPtcpCountByInstAndSubject() {
//        return null;
        Stream<Course> stream = courses.stream();
        Map<String,Integer> map = stream.collect(Collectors.groupingBy(course -> course.institution+"-"+course.subject,Collectors.summingInt(c->c.participants)));
        LinkedHashMap<String,Integer> linkedHashMap = map.entrySet().stream().sorted((a,b)->-a.getValue().compareTo(b.getValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,(x,y)->y,LinkedHashMap::new));
        return linkedHashMap;
    }

    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        System.out.println(courses.get(1).instructors);
        Map<String, List<Set<String>>> result = new HashMap<>();

        //filter unique instructor
        for (Course course : courses) {
            String[] insts = course.instructors.split(", ");

            for (String instructor : insts) {
                Set<String> independent = new TreeSet<>();
                Set<String> co = new TreeSet<>();
                List<Set<String>> lists = new ArrayList<>();
                lists.add(independent);
                lists.add(co);
                result.put(instructor, lists);
            }
        }
        // put into list
        for (Course course : courses) {
            String[] insts = course.instructors.split(", ");

            if (insts.length <= 1) { // independent
                Set<String> entry = result.get(insts[0]).get(0);
                entry.add(course.title);

            } else {
                for (String ins : insts) {
                    Set<String> entry = result.get(ins).get(1);
                    entry.add(course.title);
                }
            }
            //sort list

        }

        Map<String,List<List<String>>> res = new HashMap<>();

        for (Map.Entry<String,List<Set<String>>> list : result.entrySet()){
            TreeSet<String> set1 = (TreeSet<String>) list.getValue().get(0);
            TreeSet<String> set2 = (TreeSet<String>) list.getValue().get(1);
            String key = list.getKey();
            List<List<String>> listList = new ArrayList<>();
            List<String> list1 = new ArrayList<>(set1);
            List<String> list2 = new ArrayList<>(set2);
            listList.add(list1);
            listList.add(list2);
            res.put(key,listList);
        }
        return res;

//        return result;


    }

    //4
    public List<String> getCourses(int topK, String by) {
        if(by.equals("hours")){
            return courses.stream().sorted((c1,c2)->-Double.compare(c1.totalHours,c2.totalHours)).map(c->c.title).distinct().limit(topK).toList();
        }else if(by.equals("participants")){
            return courses.stream().sorted((c1,c2)->-Integer.compare(c1.participants,c2.participants)).map(c->c.title).distinct().limit(topK).toList();
        }
        return null;
    }


    //5
    public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
//        return null;
        Set<String> res = new TreeSet<>();
        String match = courseSubject.toLowerCase();
        for (int i = 0; i < courses.size(); i++) {
            Course course = courses.get(i);
            if(course.subject.toLowerCase().contains(match) && course.percentAudited>=percentAudited && course.totalHours<=totalCourseHours){
                res.add(course.title);
            }
        }
        return new ArrayList<>(res);
    }

    //6
    public class CourseStats{
        double averageMedian;
        double averageMale;
        double averageDegree;
        String courseId;
        String courseName;
        List<Course> courses= new ArrayList<>();

        public CourseStats(String id,String courseName){
            courseId = id;
            this.courseName = courseName;
        }
        public CourseStats(Course course){
            courses.add(course);
        }

        public void calculateStats(){
            double sumMedian = 0,sumMale = 0,sumDegree=0;
            for (int i = 0; i < courses.size(); i++) {
                Course course = courses.get(i);
                sumMale+=course.percentMale;
                sumMedian+=course.medianAge;
                sumDegree+=course.percentDegree;
            }
            averageDegree = sumDegree/courses.size();
            averageMale = sumMale/courses.size();
            averageMedian = sumMedian/courses.size();
        }
    }
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {

        Stream<Course> distinct =  courses.stream().distinct();
        Map<String,CourseStats> statsMap = new HashMap<>(); // (id, stats)
        distinct.forEach((c)->statsMap.put(c.number,new CourseStats(c.number,c.title)));
        courses.forEach((c)->statsMap.get(c.number).courses.add(c));
        statsMap.values().forEach(CourseStats::calculateStats);
        Map<String,CourseStats> rankedMap= statsMap.entrySet().stream().
                sorted(Comparator.comparingDouble(c -> calcSim(c.getValue(), age, gender, isBachelorOrHigher))).
                collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue,(x,y)->y,LinkedHashMap::new));
        return rankedMap.values().stream().map(c->c.courseName).distinct().limit(10).toList();
//        return null;
    }

    static double calcSim(CourseStats courseStats, int age, int gender, int degree){
        return Math.pow((age- courseStats.averageMedian),2)
                +Math.pow((gender*100- courseStats.averageMale),2)
                +Math.pow((100*degree- courseStats.averageDegree),2);
    }

}

class Course {
    String institution;
    String number;
    Date launchDate;
    String title;
    String instructors;
    String subject;
    int year;
    int honorCode;
    int participants;
    int audited;
    int certified;
    double percentAudited;
    double percentCertified;
    double percentCertified50;
    double percentVideo;
    double percentForum;
    double gradeHigherZero;
    double totalHours;
    double medianHoursCertification;
    double medianAge;
    double percentMale;
    double percentFemale;
    double percentDegree;

    public Course(String institution, String number, Date launchDate,
                  String title, String instructors, String subject,
                  int year, int honorCode, int participants,
                  int audited, int certified, double percentAudited,
                  double percentCertified, double percentCertified50,
                  double percentVideo, double percentForum, double gradeHigherZero,
                  double totalHours, double medianHoursCertification,
                  double medianAge, double percentMale, double percentFemale,
                  double percentDegree) {
        this.institution = institution;
        this.number = number;
        this.launchDate = launchDate;
        if (title.startsWith("\"")) title = title.substring(1);
        if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
        this.title = title;
        if (instructors.startsWith("\"")) instructors = instructors.substring(1);
        if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
        this.instructors = instructors;
        if (subject.startsWith("\"")) subject = subject.substring(1);
        if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
        this.subject = subject;
        this.year = year;
        this.honorCode = honorCode;
        this.participants = participants;
        this.audited = audited;
        this.certified = certified;
        this.percentAudited = percentAudited;
        this.percentCertified = percentCertified;
        this.percentCertified50 = percentCertified50;
        this.percentVideo = percentVideo;
        this.percentForum = percentForum;
        this.gradeHigherZero = gradeHigherZero;
        this.totalHours = totalHours;
        this.medianHoursCertification = medianHoursCertification;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentFemale = percentFemale;
        this.percentDegree = percentDegree;
    }
}