package edu.illinois.cs.cogcomp.testPackage;

import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.annotation.AnnotatorService;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.io.IOUtils;
import edu.illinois.cs.cogcomp.core.io.LineIO;
import edu.illinois.cs.cogcomp.core.utilities.SerializationHelper;
import edu.illinois.cs.cogcomp.core.utilities.TextCleaner;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.curator.CuratorConfigurator;
import edu.illinois.cs.cogcomp.curator.CuratorFactory;
import edu.illinois.cs.cogcomp.nlp.utilities.StringCleanup;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * A test file to show how to use the {@link CuratorFactory}
 * @author mssammon
 */
public class CuratorClientExample {
    private static final String NAME = CuratorClientExample.class.getCanonicalName();

    public static void main(String[] args) throws IOException, AnnotatorException {
        if (args.length != 2) {
            System.err.println("Usage: " + NAME + " inFile outDir");
            System.exit(-1);
        }
        String fileName = args[0];
        String outDir = args[1];
        if (!IOUtils.exists(outDir)) {
            IOUtils.mkdir(outDir);
            System.out.println("Created output directory '" + outDir + "'. ");
        } else if (!IOUtils.isDirectory(outDir))
            throw new IllegalArgumentException("Named directory '" + outDir + "' already exists and is not a directory.");
        // Create the AnnotatorService object
        AnnotatorService annotator = null;
        Properties props = new Properties();
        props.setProperty(CuratorConfigurator.CURATOR_HOST.key, "your.machine.name");
        props.setProperty(CuratorConfigurator.CURATOR_PORT.key, "9010"); //or whatever port your curator instance uses
        props.setProperty(CuratorConfigurator.CACHE_FORCE_UPDATE.key, CuratorConfigurator.FALSE);
        ResourceManager rm = new ResourceManager(props);
        List<String> viewList = Arrays.asList(ViewNames.POS, ViewNames.SHALLOW_PARSE, ViewNames.NER_CONLL,
                ViewNames.NER_ONTONOTES, ViewNames.PARSE_STANFORD, ViewNames.DEPENDENCY_STANFORD, ViewNames.SRL_NOM,
                ViewNames.SRL_VERB, ViewNames.PARSE_CHARNIAK);

        Set<String> viewsToAdd = new HashSet<>();
        viewsToAdd.addAll(viewList);

        try {
            annotator = CuratorFactory.buildCuratorClient(new CuratorConfigurator().getConfig(rm));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String fileStem = Paths.get(fileName).getName(Paths.get(fileName).getNameCount() - 1).toString();

        boolean isText = true;
        boolean forceOverwrite = true;
        boolean useJson = true;

        List<String> lines = LineIO.read(fileName);
        // Create a new TextAnnotation object. This will tokenize and split the sentences
        // (it will create the TOKENS and SENTENCE views).
        int num = 0;

        for (String line : lines) {
            if ( line.trim().equals("") )
                continue;

            line = StringCleanup.normalizeToAscii(line);
            line = TextCleaner.replaceXmlTags(TextCleaner.replaceDuplicatePunctuation(TextCleaner.replaceMisusedApostropheSymbol(line)));
            String id = Integer.toString(num) + (isText ? "_t" : "_h");
            System.out.println("processing example '" + id + "'...");

            if (!isText)
                num++;
            isText = !isText;

            TextAnnotation ta = annotator.createAnnotatedTextAnnotation(fileStem, id, line, viewsToAdd);

            String outFile = outDir + "/" + id + ".json";

            System.out.println("writing output to file '" + outFile + "'..." );
            SerializationHelper.serializeTextAnnotationToFile(ta, outFile, forceOverwrite, useJson);
        }
    }
}
