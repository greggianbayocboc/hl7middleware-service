package com.hisd3.utils;

import org.apache.commons.cli.*;

import static spark.Spark.get;

public class Application {

    public static void bootstrap(){


    }

    public static void main(String[] args) throws ParseException {



        Options options = new Options();
        options.addOption("t", false, "display current time");
        options.addOption("c", true, "country code");
        options.addOption("listen_dir", true, "remote directory path");
        options.addOption("start", false, "start hl7 rest service");

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "[jarfile] options", options );

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( options, args);

        String countryCode = cmd.getOptionValue("c");

        if(countryCode == null) {
            // print default date

        }
        else {
            // print date for country specified by countryCode
            System.out.println("Country Code Specified");
        }


        if(cmd.hasOption("t")) {
            // print the date and time
            System.out.println("Has T Specified");
        }
        else {
            // print the date
        }

        if(cmd.hasOption("start")) {
            bootstrap();
            get("/ping", (req, res) -> "OK");
        }

    }

}
