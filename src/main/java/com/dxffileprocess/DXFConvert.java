package com.dxffileprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import org.kabeja.batik.tools.SAXPDFSerializer;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;
import org.kabeja.svg.SVGGenerator;
import org.kabeja.xml.SAXGenerator;
import org.kabeja.xml.SAXSerializer;
import org.kabeja.xslt.SAXXMLSerializer;
import org.xml.sax.SAXException;

public class DXFConvert {
	/**
	 * @Title: GetSourceFile
	 * @Description: TODO 通过路径获取文件，并转化为DXFDocument
	 * @param filePath 源文件路径
	 * @return: DXFDocument
	 */
	public static DXFDocument GetSourceFile(String filePath) {
		Parser dxfParser = ParserBuilder.createDefaultParser();
		// 需要转换的dxf文件
		try {
			dxfParser.parse(new FileInputStream(filePath), "UTF-8");
		} catch (FileNotFoundException e) {
			System.out.println("File Not Exist!");
		} catch (ParseException e) {
			System.out.println("Parse Failed");
		}
		DXFDocument doc = dxfParser.getDocument();
		return doc;
	}

	/**
	 * @Title: DXF2SVG
	 * @Description: TODO DXF -> SVG(XML)
	 * @param doc         DXF文档
	 * @param outFilePath 输出路径
	 * @throws FileNotFoundException
	 * @throws SAXException
	 * @return: void
	 */
	public static void DXF2SVG(DXFDocument doc, String outFilePath) throws FileNotFoundException, SAXException {
		SAXGenerator generator = new SVGGenerator();
		// 输出xml
		SAXSerializer out = new SAXXMLSerializer();
		OutputStream fileOS = new FileOutputStream(outFilePath);
		// 设置输出路径
		out.setOutput(fileOS);
		// 输出
		generator.generate(doc, out, new HashMap());
		System.out.println("DXF to SVG Done");
	}

	/**
	 * @Title: ChangeNameToPieceName
	 * @Description: TODO 适用于单个pieceName
	 * @param filePath svg文件路径
	 * @throws FileNotFoundException
	 * @return: void
	 */
	public static void ChangeNameToPieceName(String filePath) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(filePath));
		String line = null;
		String str = "";
		while ((line = in.readLine()) != null)
			str += line;
		in.close();
		int i = str.indexOf("Piece Name");
		String pieceName = "null";
		if (i > -1)
			pieceName = str.substring((i + 12), (i + 23));
		// svg文件重命名
		File oldF = new File(filePath);
		if (oldF.exists()) {
			// 文件路径：D:/Projects/Project1_Convert/singlePic.xml
			filePath = filePath.substring(0, filePath.lastIndexOf('/') + 1) + pieceName + ".svg";
			File newF = new File(filePath);
			oldF.renameTo(newF);
		}
	}

	/**
	 * @Title: DXF2PDF
	 * @Description: TODO DXF -> PDF，效果清晰
	 * @param doc      DXF文档
	 * @param fileName
	 * @throws FileNotFoundException
	 * @throws SAXException
	 * @return: void
	 */
	public static void DXF2PDF(DXFDocument doc, String fileName) throws FileNotFoundException, SAXException {
		SAXGenerator generator = new SVGGenerator();
		// 输出pdf
		SAXSerializer out = new SAXPDFSerializer();
		OutputStream fileOS = new FileOutputStream("D:/Projects/Project1_Convert/" + fileName + ".pdf"); // 清晰
		// 设置输出路径
		out.setOutput(fileOS);
		generator.generate(doc, out, new HashMap());
		System.out.println("DXF to PDF Done");
	}
}
