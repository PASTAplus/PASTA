package com.jeldoclet;

/*
 * File: JELDoclet.java
 * Purpose: A Doclet to be used with JavaDoc which will output XML with all of the information
 *    from the JavaDoc.
 * Date: Mar 2nd, 2003
 * 
 * History:
 * 		Sep 14th, 2005 - Updated by TP to allow multiple file output.
 * 					   - Added support for exceptions.
 * 					   - Added support for a few missing method modifiers (final, etc).
 *                     - Added support for xml namespaces.
 *                     
 *    Dec 8/9th, 2005 - updated by T.Zwolsky (all extensions marked thz.../thz):
 *      - added cmdline parameter -outputEncoding
 *      - bugfix: implements/interface node(s) were created but not inserted into class node
 *      - added exception comment (both here and in the xsd as optional element)
 *      - added cmdline parameter -filename
 *      - added output directory check
 *      - added some comments to readme
 *      - added test target in build.xml
 *      - added nested class in test classes (test/MyInterClass.java)
 *      - added xsl transformation jel2html.xsl
 *      
 *      Dec 16th, 2005 - updated by T.Zwolsky (all extensions marked thz.../thz):
 *      - added version here and in xsd
 *      - added admin stuff in xsd
 * 
 * Author: Jack D. Herrington <jack_d_herrington@codegeneration.net>
 * 		   Toby Patke 		  <toby_patke _?_ hotmail.com>
 * 
 * This source is covered by the Open Software Licenses (1.1)
 */

import com.sun.javadoc.*;
import com.sun.javadoc.AnnotationDesc.ElementValuePair;

import java.util.Date;

/**
 * The core JELDoclet class.
 * 
 * @author Jack D. Herrington, Toby Patke
 */
public class JELDoclet {

    private static String version = "jeldoclet-2.0";
    
    /**
     * Determines whether the output is a single file or multiple files.
     * Populated from the command line via the "-multiple" flag.
     */
    private static boolean multipleFiles = false;

    /**
     * Determines the directory where output is placed. Populated from the
     * command line via the "-d [directory]" flag.
     */
    private static String outputDirectory = "./";

    /**
     * Determines whether the generated output is part of a namespace. Populated
     * from the command line via the "-includeNamespace" flag.
     */
    private static boolean includeNamespace = false;

    /**
     * (thz) determines standard / adjustable encoding
     */
    private static String outputEncoding = "UTF-8";

    /**
     * (thz) single output: this is the filename, multiple output: this is
     * prepended to the filename created; default is empty (single: "jel.xml",
     * multiple: package.class.xml) !
     */
    private static String filenameBase = "";

    /**
     * Processes the JavaDoc documentation.
     * 
     * @param root
     *            The root of the documentation tree.
     * @return True if processing was succesful.
     * @see com.sun.java.Doclet
     */
    public static boolean start(RootDoc root) {
        // Get program options
        getOptions(root);

        // Create the root node.
        XMLNode[] nodes = buildXmlFromDoc(root);

        // Save the output XML
        save(nodes);

        return true;
    }

    /**
     * A JavaDoc option parsing handler. This one returns the number of
     * arguments required for the given option.
     * 
     * @param option
     *            The name of the option.
     * @return The number of arguments.
     * @see com.sun.java.Doclet
     */
    public static int optionLength(String option) { // This method is required
                                                    // by the doclet spec.

        // Check for our flags. Return the number of parameters expected after a
        // given flag (including the flag).

        if (option.compareToIgnoreCase("-multiple") == 0)
            return 1;

        if (option.compareToIgnoreCase("-includeNamespace") == 0)
            return 1;

        if (option.compareToIgnoreCase("-d") == 0)
            return 2;

        // thz
        if (option.compareToIgnoreCase("-outputEncoding") == 0)
            return 2;
        if (option.compareToIgnoreCase("-filename") == 0)
            return 2;
        // /thz

        return 0;
    }

    /**
     * A JavaDoc option parsing handler. This one checks the validity of the
     * options.
     * 
     * @param options
     *            The two dimensional array of options.
     * @param reporter
     *            The error reporter.
     * @return True if the options are valid.
     * @see com.sun.java.Doclet
     */
    public static boolean validOptions(String options[][],
                                       DocErrorReporter reporter) { 
        // This method is required by the doclet spec. If we had any options
        // to validate, we would do it here.
        return true;
    }

    /**
     * Retrieve the expected options from the given array of options.
     * 
     * @param root
     *            The root object which contains the options to be retrieved.
     */
    private static void getOptions(RootDoc root) {
        // Get the file name and determine if multiple files should be built.

        String[][] options = root.options();
        for (int opt = 0; opt < options.length; opt++) {
            if (options[opt][0].compareToIgnoreCase("-d") == 0) {
                outputDirectory = options[opt][1];
                // thz: make sure output directory ends with a path separator
                // (the default does)
                String fs = System.getProperty("file.separator");
                if (outputDirectory.endsWith(fs) == false)
                    outputDirectory += fs;
                // /thz
                continue;
            }

            if (options[opt][0].compareToIgnoreCase("-multiple") == 0) {
                multipleFiles = true;
                continue;
            }

            if (options[opt][0].compareToIgnoreCase("-includeNamespace") == 0) {
                includeNamespace = true;
                continue;
            }

            // thz
            if (options[opt][0].compareToIgnoreCase("-outputEncoding") == 0) {
                outputEncoding = options[opt][1];
                continue;
            }
            if (options[opt][0].compareToIgnoreCase("-filename") == 0) {
                filenameBase = options[opt][1];
                continue;
            }
            // /thz
        }

        System.out.println("Doclet: " + version);
        System.out.println("Output directory: " + outputDirectory);
        System.out.println("Output encoding: " + outputEncoding);
        System.out.println("Saving as "
                + (multipleFiles ? "multiple files." : "a single file."));

        if (!filenameBase.isEmpty())
            System.out.println("filename " + (multipleFiles ? " base " : "")
                    + ": '" + filenameBase + "'");
    }

    /**
     * Save the given array of nodes. Will either save the files individually,
     * or as a single file depending on the existence of the "-multiple" flag.
     * 
     * @param nodes
     *            The array of nodes to be saved.
     */
    private static void save(XMLNode[] nodes) {

        if (multipleFiles) {
            saveAsMultipleFiles(nodes);
        } else {
            saveAsSingleFile(nodes);
        }
    }
    
    private static void saveAsMultipleFiles(XMLNode[] nodes) {
        
        for (int index = 0; index < nodes.length; index++) {

            XMLNode xml = makeRootXMLNode();
            xml.addNode(nodes[index]);

            String fileName = makeFileName(nodes[index]);
            
            saveAsFile(xml, fileName);
        }
    }
    
    private static String makeFileName() {
        return filenameBase.isEmpty() ? "jel.xml" : filenameBase;
    }
    
    private static String makeFileName(XMLNode node) {
        return filenameBase + node.getAttribute("fulltype") + ".xml";
    }

    private static void saveAsFile(XMLNode xml, String fileName) {
        xml.save(outputDirectory, fileName, includeNamespace, outputEncoding);
    }
    
    private static void saveAsSingleFile(XMLNode[] nodes) {

        XMLNode xml = makeRootXMLNode();
        
        for (int index = 0; index < nodes.length; index++) {
            xml.addNode(nodes[index]);
        }
        
        String fileName = makeFileName();

        saveAsFile(xml, fileName);
    }
    
    private static XMLNode makeRootXMLNode() {

        XMLNode rootXml = new XMLNode("java-to-xml");

        rootXml.addAttribute("doclet", version);
        rootXml.addAttribute("creation", new Date().toString());

        return rootXml;
    }
    

    /**
     * Builds the XML nodes from a given com.sun.javadoc.RootDoc.
     * 
     * @param root
     *            The RootDoc from which the XML should be built.
     * @return The array of XML nodes which represents the RootDoc.
     */
    private static XMLNode[] buildXmlFromDoc(RootDoc root) {
        // Iterate through all of the classes and generate a node for each
        // class.
        // NOTE: Nodes may contain subnodes if classes contain subclasses.

        ClassDoc[] classes = root.classes();
        XMLNode[] retval = new XMLNode[classes.length];

        for (int index = 0; index < classes.length; index++) {
            retval[index] = transformClass(classes[index], null);
        }

        return retval;
    }

    /**
     * Transforms the provided annotation descriptions into XML.
     * 
     * @param annDescs
     *            the annotation descriptions.
     * @param node
     *            The node to add the comment nodes to.
     */
    private static void transformAnnotations(AnnotationDesc[] annDescs, 
                                             XMLNode node) {
        for (AnnotationDesc desc : annDescs) {
            transformAnnotation(desc, node);
        }
    }

    private static void transformAnnotation(AnnotationDesc desc, XMLNode node) {

        XMLNode annotationNode = new XMLNode("annotation");
        
        AnnotationTypeDoc type = desc.annotationType();

        annotationNode.addAttribute("fulltype", type.qualifiedName());
        annotationNode.addAttribute("type", type.simpleTypeName());
        
        for (ElementValuePair pair : desc.elementValues()) {
            transformElementValuePair(pair, annotationNode);
        }
        
        node.addNode(annotationNode);
    }
    
    private static void transformElementValuePair(ElementValuePair pair,
                                                  XMLNode node) {

        XMLNode elementValuePairNode = new XMLNode("elementValuePair");
        
        String elementName = pair.element().name();
        elementValuePairNode.addAttribute("element", elementName);
        
        String value = pair.value().toString();
        elementValuePairNode.addAttribute("value", value);

        node.addNode(elementValuePairNode);
    }
    
    /**
     * Transforms comments on the Doc object into XML.
     * 
     * @param doc
     *            The Doc object.
     * @param node
     *            The node to add the comment nodes to.
     */
    private static void transformComment(Doc doc, XMLNode node) {
        // Creat the comment node

        XMLNode commentNode = new XMLNode("comment");
        boolean addNode = false;

        // Handle the basic comment

        if (doc.commentText() != null && doc.commentText().length() > 0) {
            commentNode.addText(doc.commentText());
            addNode = true;
        }

        // Handle the tags

        Tag[] tags = doc.tags();
        for (int tag = 0; tag < tags.length; tag++) {
            XMLNode paramNode = new XMLNode("attribute");
            paramNode.addAttribute("name", tags[tag].name());
            paramNode.addText(tags[tag].text());

            commentNode.addNode(paramNode);

            addNode = true;
        }

        // Add the node to the host

        if (addNode)
            node.addNode(commentNode);
    }

    /**
     * Creates a <fields> node from a set of fields.
     * 
     * @param fields
     *            The set of fields.
     * @param node
     *            The node to add the <field> nodes to.
     */
    private static void transformFields(FieldDoc[] fields, XMLNode node) {
        if (fields.length < 1)
            return;

        // Create the <fields> node and iterate through the fields

        XMLNode fieldsNode = new XMLNode("fields");
        for (int index = 0; index < fields.length; index++) {
            // Create the <field> node and populate it.

            XMLNode fieldNode = new XMLNode("field");

            fieldNode.addAttribute("name", fields[index].name());
            fieldNode.addAttribute("type", fields[index].type().typeName());
            fieldNode.addAttribute("fulltype", fields[index].type().toString());

            if (fields[index].constantValue() != null
                    && fields[index].constantValue().toString().length() > 0)
                fieldNode.addAttribute("const", fields[index].constantValue()
                        .toString());

            if (fields[index].constantValueExpression() != null
                    && fields[index].constantValueExpression().length() > 0)
                fieldNode.addAttribute("constexpr", fields[index]
                        .constantValueExpression());

            setVisibility(fields[index], fieldNode);

            if (fields[index].isStatic())
                fieldNode.addAttribute("static", "true");

            if (fields[index].isFinal())
                fieldNode.addAttribute("final", "true");

            if (fields[index].isTransient())
                fieldNode.addAttribute("transient", "true");

            if (fields[index].isVolatile())
                fieldNode.addAttribute("volatile", "true");

            // Add comments attached to the field.

            transformComment(fields[index], fieldNode);

            // Add the <field> node to the <fields> node.

            fieldsNode.addNode(fieldNode);
        }

        // Add the <fields> node to the host.
        node.addNode(fieldsNode);
    }

    /**
     * Sets the visibility for the class, method or field.
     * 
     * @param member
     *            The member for which the visibility needs to be set (class,
     *            method, or field).
     * @param node
     *            The node to which the visibility should be set.
     */
    private static void setVisibility(ProgramElementDoc member, XMLNode node) {
        if (member.isPrivate())
            node.addAttribute("visibility", "private");

        else if (member.isProtected())
            node.addAttribute("visibility", "protected");

        else if (member.isPublic())
            node.addAttribute("visibility", "public");

        else if (member.isPackagePrivate())
            node.addAttribute("visibility", "package-private");
    }

    /**
     * Populates the interior of a <method> node from information from an
     * ExecutableMemberDoc object.
     * 
     * @param method
     *            The method documentation.
     * @param node
     *            The node to add the XML to.
     */
    private static void populateMethodNode(ExecutableMemberDoc method,
            XMLNode node) {
        // Add any comments associated with the method

        transformComment(method, node);

        // Add any annotations associated with the method
        
        transformAnnotations(method.annotations(), node);
        
        // Add the basic values

        node.addAttribute("name", method.name());

        setVisibility(method, node);

        if (method.isStatic())
            node.addAttribute("static", "true");

        if (method.isInterface())
            node.addAttribute("interface", "true");

        if (method.isFinal())
            node.addAttribute("final", "true");

        if (method instanceof MethodDoc)
            if (((MethodDoc) method).isAbstract())
                node.addAttribute("abstract", "true");

        if (method.isSynchronized())
            node.addAttribute("synchronized", "true");

        if (method.isSynthetic())
            node.addAttribute("synthetic", "true");

        // Iterate through the parameters and add them

        Parameter[] params = method.parameters();

        if (params.length > 0) {
            ParamTag[] paramTags = method.paramTags();
            XMLNode paramsNode = new XMLNode("params");

            for (int param = 0; param < params.length; param++) {
                XMLNode paramNode = new XMLNode("param");

                paramNode.addAttribute("name", params[param].name());
                paramNode.addAttribute("type", params[param].type().typeName());
                paramNode.addAttribute("fulltype", params[param].type()
                        .toString());

                for (int paramTag = 0; paramTag < paramTags.length; paramTag++) {
                    if (paramTags[paramTag].parameterName()
                            .compareToIgnoreCase(params[param].name()) == 0) {
                        paramNode.addAttribute("comment", paramTags[paramTag]
                                .parameterComment());
                    }
                }

                paramsNode.addNode(paramNode);
            }

            node.addNode(paramsNode);
        }

        // Iterate through the exceptions and add them

        ClassDoc[] exceptions = method.thrownExceptions();

        // thz: @throws/@exception tags
        ThrowsTag[] exceptionTags = method.throwsTags();

        if (exceptions != null && exceptions.length > 0) {
            XMLNode exceptionsNode = new XMLNode("exceptions");

            for (int except = 0; except < exceptions.length; except++) {
                XMLNode exceptNode = new XMLNode("exception");

                exceptNode.addAttribute("type", exceptions[except].typeName());
                exceptNode.addAttribute("fulltype", exceptions[except]
                        .qualifiedTypeName());

                // thz
                for (int exceptionTag = 0; exceptionTag < exceptionTags.length; exceptionTag++) {
                    if (exceptionTags[exceptionTag].exceptionName()
                            .compareToIgnoreCase(exceptions[except].typeName()) == 0)
                        exceptNode.addAttribute("comment",
                                exceptionTags[exceptionTag].exceptionComment());
                }
                // /thz

                exceptionsNode.addNode(exceptNode);
            }

            node.addNode(exceptionsNode);
        }

        // if (method.thrownExceptions())
    }

    /**
     * Transforms an array of methods and an array of constructor methods into
     * XML and adds those to the host node.
     * 
     * @param methods
     *            The methods.
     * @param constructors
     *            The constructors.
     * @param node
     *            The node to add the XML to.
     */
    private static void transformMethods(MethodDoc[] methods,
            ConstructorDoc[] constructors, XMLNode node) {
        if (methods.length < 1 && constructors.length < 1)
            return;

        // Create the <methods> node

        XMLNode methodsNode = new XMLNode("methods");

        // Add the <constructor> nodes

        for (int index = 0; index < constructors.length; index++) {
            XMLNode constNode = new XMLNode("constructor");

            populateMethodNode(constructors[index], constNode);

            methodsNode.addNode(constNode);
        }

        // Add the <method> nodes

        for (int index = 0; index < methods.length; index++) {
            XMLNode methodNode = new XMLNode("method");

            populateMethodNode(methods[index], methodNode);

            methodNode.addAttribute("type", methods[index].returnType()
                    .typeName());
            methodNode.addAttribute("fulltype", methods[index].returnType()
                    .toString());

            Tag[] returnTags = methods[index].tags("@return");
            if (returnTags.length > 0) {
                methodNode.addAttribute("returncomment", returnTags[0].text());
            }

            methodsNode.addNode(methodNode);
        }

        // Add the <methods> node to the host

        node.addNode(methodsNode);
    }

    /**
     * Transforms a ClassDoc class into XML and adds it to the root XML node.
     * 
     * @param classDoc
     *            The class to transform.
     * @param root
     *            The XML node to add the class XML to.
     */
    private static XMLNode transformClass(ClassDoc classDoc, XMLNode root) {
        XMLNode classNode = new XMLNode("jelclass"); // "class" needs a prefix
                                                     // for output to work with
                                                     // JAXB.

        // Handle basic class attributes

        setVisibility(classDoc, classNode);

        classNode.addAttribute("type", classDoc.name());
        classNode.addAttribute("fulltype", classDoc.qualifiedName());
        classNode.addAttribute("package", classDoc.containingPackage().name());

        ClassDoc[] extendClasses = classDoc.interfaces();
        if (extendClasses.length > 0) {
            XMLNode implement = new XMLNode("implements");
            for (int extendIndex = 0; extendIndex < extendClasses.length; extendIndex++) {
                XMLNode interfce = new XMLNode("interface");
                interfce
                        .addAttribute("type", extendClasses[extendIndex].name());
                interfce.addAttribute("fulltype", extendClasses[extendIndex]
                        .qualifiedName());
                implement.addNode(interfce);
            }

            // thz: implements-node should be inserted, too
            classNode.addNode(implement);
            // /thz
        }

        if (classDoc.superclass() != null) {
            classNode.addAttribute("superclass", classDoc.superclass().name());
            classNode.addAttribute("superclassfulltype", classDoc.superclass()
                    .qualifiedName());
        }

        if (classDoc.isInterface())
            classNode.addAttribute("interface", "true");

        if (classDoc.isFinal())
            classNode.addAttribute("final", "true");

        if (classDoc.isAbstract())
            classNode.addAttribute("abstract", "true");

        if (classDoc.isSerializable())
            classNode.addAttribute("serializable", "true");

        // Handle the comments on the class

        transformComment(classDoc, classNode);

        // Handle the annotations on the class
        
        transformAnnotations(classDoc.annotations(), classNode);
        
        // Handle the fields

        transformFields(classDoc.fields(), classNode);

        // Handle the methods

        transformMethods(classDoc.methods(), classDoc.constructors(), classNode);

        // Handle inner classes

        ClassDoc[] innerClasses = classDoc.innerClasses();
        for (int classIndex = 0; classIndex < innerClasses.length; classIndex++)
            classNode.addNode(transformClass(innerClasses[classIndex],
                    classNode));

        return classNode;
    }
}
