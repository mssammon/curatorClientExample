# Curator Client Example

This is a simple project to demonstrate use of the illinois-curator 
package. It requires that you have an instance of curator set up on
a machine you can access via network. 

You will need to set the following values to the host and port for
your curator instance:

        props.setProperty(CuratorConfigurator.CURATOR_HOST.key, "your.machine.name");
        props.setProperty(CuratorConfigurator.CURATOR_PORT.key, "9010"); //or whatever port your curator instance uses

```AnnotationTest.main()``` takes two arguments: a text file
and an output directory. It processes one line at a time from the 
text file and creates a corresponding output file in .json format
in the output directory named by the second argument. 

This is an idiosyncratic setup because I used it to process a plain
text Entailment corpus file, which had each pair in order with the
Text, then the Hypothesis each on a single line, separated by one or
more empty lines.  The Curator can handle longer text inputs, though
it will take longer (and may time out) if you are using complex
annotators like SRL or Coreference and feed it long documents. 
Some of the annotators also tend to fail on very long sentences,
due to the need for a syntactic parse of the input. 