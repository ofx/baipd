package nl.uu.cs.arg.persuasion.platform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;

import nl.uu.cs.arg.persuasion.platform.local.BaipdXmlData;

public class PersuasionLauncher {

    Thread platformThread;

    PersuasionPlatform platform;

    BaipdXmlData baipdXml;

    public static void main(String[] args) {

        // Create a parser for the command line arguments
        OptionParser parser = new OptionParser();
        ArgumentAcceptingOptionSpec<PersuasionPlatformOutputLevel> levelOption = parser.
                acceptsAll(java.util.Arrays.asList("l", "level"), "Set message console print level {Exceptions, Messages, Moves}").
                withRequiredArg().ofType(PersuasionPlatformOutputLevel.class);
        OptionSpecBuilder helpOption = parser.acceptsAll(java.util.Arrays.asList("?", "h", "help"), "Print this usage message");
        OptionSpecBuilder versionOption = parser.acceptsAll(java.util.Arrays.asList("v", "version"), "Version info");

        try {

            // Parse the arguments
            OptionSet options = null;
            try {
                options = parser.parse(args);
            } catch (OptionException e) {
                printCommandLineUsage(parser, e.getMessage());
                return;
            }

            // Usage or version information
            if (options == null || options.has(helpOption)) {
                printCommandLineUsage(parser);
                return;
            } else if (options.has(versionOption)) {
                System.out.println(PersuasionSettings.APPLICATION_NAME_VERSION);
                return;
            }

            // Build platform settings object from the supplied command line options
            List<PersuasionPlatformListener> listeners = new ArrayList<PersuasionPlatformListener>();

            // Console message printing level
            try {
                if (options.hasArgument(levelOption)) {
                    listeners.add(PersuasionPlatformOutputPrinter.defaultPlatformOutputPrinter);
                    PersuasionPlatformOutputPrinter.defaultPlatformOutputPrinter.setLevel(levelOption.value(options));
                }
            } catch (OptionException e) {
                printCommandLineUsage(parser, e.getMessage());
                return;
            }

            // Check remaining arguments for a MAS filename
            List<String> noargs = options.nonOptionArguments();
            File masFile = null;
            if (noargs != null && noargs.size() > 0) {
                masFile = new File(noargs.get(0));
            }
            if (masFile == null || !masFile.exists()) {
                // No MAS file specification supplied; print out help info
                printCommandLineUsage(parser, "No .baidd MAS XML file specified or file could not be found.");
                return;
            }

            // Read the MAS XML file (and agent XML files that are specified there)
            BaipdXmlData baipdXml = null;
            try {
                baipdXml = BaipdXmlData.loadAgentDataFromXml(masFile);
            } catch (Exception e) {
                System.out.println("An error occured during the parsing of the MAS and agent XML files.");
                e.printStackTrace();
                return;
            }

            // Copy the read settings to the platform settings object
            PersuasionSettings settings = new PersuasionSettings(baipdXml.getPersuasionRules(),
                    baipdXml.getTerminationRules(), baipdXml.getOutcomeSelectionRule());

            // Start the agent platform
            PersuasionLauncher launcher = new PersuasionLauncher(settings, baipdXml, listeners);
            launcher.initPlatform();
            launcher.startPlatform();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void printCommandLineUsage(OptionParser parser) throws IOException {
        printCommandLineUsage(parser, null);
    }

    private static void printCommandLineUsage(OptionParser parser, String startupErrorMessage) throws IOException {

        // Application welcome message
        System.out.println(PersuasionSettings.APPLICATION_NAME_VERSION);
        char[] hyphens = new char[PersuasionSettings.APPLICATION_NAME_VERSION.length()];
        Arrays.fill(hyphens, '-'); // Used to create a hyphen --- line of the same length as the application name and version number
        System.out.println(String.valueOf(hyphens));
        System.out.println();

        // Exception to show?
        if (startupErrorMessage != null) {
            System.out.println(startupErrorMessage);
            System.out.println();
        }

        // Usage message
        String startupScriptName = "./baidd-console";
        if (File.separator.equals("\\")) { // Windows systems
            startupScriptName = "baidd-console.bat";
        }
        System.out.println("Usage:");
        System.out.println("\t" + startupScriptName + " [option]... [masfile]");
        System.out.println("For example, start the platform for some MAS specification with verbose output:");
        System.out.println("\t." + startupScriptName + " -l Moves " + File.separator + "path" + File.separator + "to" + File.separator + "TestMas.baidd");
        System.out.println();

        // Command line help directly form the parser specification
        parser.printHelpOn(System.out);

    }

    public PersuasionLauncher(PersuasionSettings settings, BaipdXmlData baipdXml, List<PersuasionPlatformListener> listeners) {
        this.platform =  new PersuasionPlatform(settings);
        this.platformThread = new Thread(platform);
        this.baipdXml = baipdXml;

        // Add platform listeners
        if (listeners != null) {
            for (PersuasionPlatformListener listener : listeners) {
                this.platform.addListener(listener);
            }
        }
    }

    public void initPlatform() {
        this.platform.init(this.baipdXml.getTopic(), this.baipdXml.getLocalAgents());
        this.platformThread.start();
    }

    public void startPlatform() {
        synchronized (this.platform) {
            try {
                // Wait until it is started and ready
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            this.platform.requestStart();
        }
    }

    public void pausePlatform() {
        synchronized (this.platform) {
            this.platform.requestPause();
        }
    }

    public boolean isPlatformPaused() {
        synchronized (this.platform) {
            return this.platform.isPaused();
        }
    }

    public void setPlatformSingleStepping(boolean stopAfterEachStep) {
        synchronized (this.platform) {
            this.platform.setSingleStepping(stopAfterEachStep);
        }
    }

}
