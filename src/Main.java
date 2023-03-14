public class Main {
    public static void main(String[] args) {
        OnlineCoursesAnalyzer onlineCoursesAnalyzer = new OnlineCoursesAnalyzer("resources/local.csv");
        onlineCoursesAnalyzer.getCourseListOfInstructor();
    }
}
