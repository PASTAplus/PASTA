package com.jeldoclet;

import java.io.FileWriter;
import java.io.IOException;

import java.util.HashMap;
import java.util.Vector;
import java.util.Iterator;
import java.util.regex.*;

/**
 * Represents an XML node
 * 
 * History:
 * 		Sep 14th, 2005 - Updated by TP to for better xml output formatting.  
 *                     - Added save method.
 *                     - Added support for a namespace (http://xml.jeldoclet.com)
 * 
 * Author: Jack D. Herrington <jack_d_herrington@codegeneration.net>
 * 		   Toby Patke 		  <toby_patke _?_ hotmail.com>
 */
public class XMLNode
{
	/**
	 * Used in the toString method to provide a carriage-return + line-feed. 
	 */
	private static final String crlf = System.getProperty("line.separator");
	
	/**
	 * The type of the node
	 */
	private String _type;
	
	/**
	 * Sets the processing instruction to be written when the object is serialized.
	 */
	private static String _processingInstructions = "";
	
	/**
	 * Sets the namespace to be written when the object is serialized.
	 */
	private static final String _namespace = "http://xml.jeldoclet.com";
	
	/**
	 * Sets the namespace prefix to be written when the object is serialized.
	 */
	private static String _namespacePrefix = "";

	/**
	 * The attributes
	 */
	private HashMap _attributes;

	/**
	 * The interior nodes
	 */
	private Vector _nodes;

	/**
	 * The interior text
	 */
	private StringBuffer _text;
	
	/**
	 * Constructs the XMLNode.
	 * 
	 * @param type The type name of the node
	 */
	public XMLNode( String type )
	{
		_type = type;
		_attributes = new HashMap();
		_nodes = new Vector();
		_text = new StringBuffer();
	}
	
	/**
	 * Adds an attribute to the node
	 * 
	 * @param name The name of the attribute.
	 * @param value The value for the attribute
	 */
	public void addAttribute( String name, String value )
	{
		_attributes.put( name, value );
	}
	
	/**
	 * Returns the specified attributed.
	 * 
	 * @param name The key for the value to be retrieved.
	 * @return The value stored in the attribute hash for the given key.
	 */
	public String getAttribute( String name )
	{
		return (String) _attributes.get( name );
	}

	/**
	 * Adds an interior node to the XMLNode.
	 * 
	 * @param node The node
	 */
	public void addNode( XMLNode node )
	{
		_nodes.add( node );
	}

	/**
	 * Adds text to the interior of the node.
	 * 
	 * @param text The node
	 */
	public void addText( String text )
	{
		_text.append( text );
	}
	

	/**
	 * thz: compatibility: original call w/o output encoding
	 */
	public void save(String dir, String fileName, boolean includeNamespace)
	{
		this.save(dir, fileName, includeNamespace, "");
	}

	/**
	 * Saves this XML node to the directory specified.
	 * 
	 * @param dir The directory to save this node to.
	 *
	 * thz: added output encoding
	 */
	public void save(String dir, String fileName, boolean includeNamespace, String outputEncoding)
	{
		try 
		{

			if(includeNamespace)
			{
				// thz
				if(outputEncoding.equals("") == true)
					outputEncoding = "UTF-8";
				// /thz

				_processingInstructions = "<?xml version=\"1.0\" encoding=\"" + outputEncoding + "\" standalone=\"yes\"?>" + crlf;
				_namespacePrefix = "xs";
				this.addAttribute("xmlns:" + _namespacePrefix, _namespace);
				_namespacePrefix = _namespacePrefix + ":";
			}
			// thz
			else
			{
				if(outputEncoding.equals("") == false)
					_processingInstructions = "<?xml version=\"1.0\" encoding=\"" + outputEncoding + "\"?>" + crlf;
			}
			// /thz
			
			FileWriter out = new FileWriter( dir + fileName );
			out.write( _processingInstructions );
			out.write( this.toString("") );
			out.close();
		}
		catch( IOException e )
		{
			System.out.println( "Could not create '" + dir + fileName + "'" );
		}
	}

	/** 
	 * Converts the XML node to a String.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString(String tabs)
	{
		StringBuffer out = new StringBuffer();
		
		out.append( tabs + "<" + _namespacePrefix + _type );
		Iterator attrIterator = _attributes.keySet().iterator();
		while( attrIterator.hasNext() )
		{
			String key = (String)attrIterator.next();
			out.append( " " + key + "=\"" + encode( (String)_attributes.get( key ) ) + "\"" );
		}

		Iterator nodeIterator = _nodes.iterator();
		
		if( _text.length() <= 0 && 
			! nodeIterator.hasNext() )
		{
			out.append( " />" + crlf); 
			return out.toString();
		}
		
		out.append( ">" + crlf);  

		if( _text.length() > 0 )
		{
			//Wrapping text in a seperate node allows for good presentation of data with out adding extra data.
			out.append( tabs + "\t<" + _namespacePrefix + "description>" + encode( _text.toString() ) + 
								"</" + _namespacePrefix + "description>" + crlf ); 
		}

		while( nodeIterator.hasNext() )
		{
			XMLNode node = (XMLNode)nodeIterator.next();
			out.append( node.toString(tabs + "\t") );
		}

		out.append( tabs + "</" + _namespacePrefix + _type + ">" + crlf  + 
				( "class".equalsIgnoreCase( _type ) ? crlf : "" ));
		
		return out.toString();
	}
	
	/** 
	 * Encodes strings as XML. Check for <, >, ', ", &.
	 * 
	 * @param in The input string
	 * @return The encoded string.
	 */
	static protected String encode( String in )
	{
		Pattern ampPat = Pattern.compile( "&" );
		Pattern ltPat = Pattern.compile( "<" );
		Pattern gtPat = Pattern.compile( ">" );
		Pattern aposPat = Pattern.compile( "\'" );
		Pattern quotPat = Pattern.compile( "\"" );

		String out = new String( in );

		out = (ampPat.matcher(out)).replaceAll("&amp;");
		out = (ltPat.matcher(out)).replaceAll("&lt;");
		out = (gtPat.matcher(out)).replaceAll("&gt;");
		out = (aposPat.matcher(out)).replaceAll("&apos;");
		out = (quotPat.matcher(out)).replaceAll("&quot;");

		return out;
	}
}
