package ru.otus.java.basic.tegneryadnov.coursework;

public class Config {
    public static String columnSeparator = ";";
    public static final int BOUND = 7;
    public static final String poisonPill = "STOP";
    public static String url = "jdbc:postgresql://localhost:5432/db_tests";
    public static final String sql = "SELECT * FROM public.answers\n" +
            "ORDER BY id ASC LIMIT 100";
    public static String reportFileName = "report.csv";
    public static boolean withHeader = true;
    public static String reporterCharSetName = "windows-1251";
    public static String login = (String)System.getProperties().getOrDefault("login", "postgres");
    public static String password = (String)System.getProperties().getOrDefault("password", "123");
}
