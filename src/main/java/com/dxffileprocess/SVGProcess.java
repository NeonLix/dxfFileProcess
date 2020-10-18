package com.dxffileprocess;

import org.apache.batik.transcoder.TranscoderException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class SVGProcess {
	// 默认存放图片文件、模板的路径
	private static String imagePath;
//	public static void main(String[] args) {
//		SVGDevide(imagePath + "/compPic.svg");
//	}
	/**
	 * @Title: SVGDevide   
	 * @Description: TODO   将一个多图的SVG按图分为svg
	 * @param filePath   SVG源文件路径
	 * @param outFilePath   输出文件路径（无具体文件，默认工程目录/images）
	 * @return: void
	 */
	public static void SVGDevide(String filePath, String outFilePath) {
		imagePath = outFilePath;
		// 1.创建Reader对象
		SAXReader reader = new SAXReader();
		// 2.加载xml
		Document document;
		try {
			document = reader.read(new File(filePath));
			WriteText(document);
			WriteLine(document);
			WritePath(document);
			EndSVG();
			UpdateNameAndViewbox();
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @Title: SVG2JPG   
	 * @Description: TODO      SVG -> JPG   
	 * @return: void
	 */
	public static void SVG2JPG() {
		try {
			SVGConvert.SVGs2JPGs(imagePath, nameList);
		} catch (IOException | TranscoderException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @Title: GetViewBoxFromPath   
	 * @Description: TODO   利用path节点的属性d中数据，计算出viewBox
	 * @param: pathNode
	 * @return: String      
	 * @throws
	 */
	private static String GetViewBoxFromPath(Element pathNode) {
		if (!pathNode.getName().equals("path"))
			return null;
		String posStrs[] = pathNode.attribute("d").getValue().toString().split("\\s");
		// posStrs格式：M -257.9927426804836 -24.15827161102091
		// 前一个数存arrayX，后一个数存arrayY
		List<Double> listX = new ArrayList<Double>();
		List<Double> listY = new ArrayList<Double>();
		boolean flag = true;
		for (String posStr : posStrs) {
			if (posStr.length() > 1) // 排除M、L等字符
			{
				if (flag)
					listX.add(Double.parseDouble(posStr));
				else
					listY.add(Double.parseDouble(posStr));
				flag = !flag;
			}
		}
		// 修改<svg>的属性ViewBox，以保证图像居中
		double minX = Collections.min(listX);
		double maxY = Collections.max(listY);
		double width = Math.abs(Collections.max(listX) - minX);
		double height = Math.abs(maxY - Collections.min(listY));
		maxY = -maxY;
		// double[] viewBox = {minX, maxY, width, height};
		// 可调整图像位置、大小
		String viewBox = (minX-1) + " " + (maxY-1) + " " + (width+5) + " " + (height+2);
		return viewBox;
	}

	/**
	 * @Title: SetViewBox   
	 * @Description: TODO  给出待设置viewBox文件，计算并设置viewBox
	 * @param document
	 * @param filePath
	 * @throws DocumentException
	 * @throws IOException      
	 * @return: void
	 */
	private static void SetViewBox(Document document, String filePath) throws DocumentException, IOException {
		Element svgNode = document.getRootElement(); // root -> svg
		String val = GetViewBoxFromPath(svgNode.element("g").element("g").element("path"));
		svgNode.attribute("viewBox").setValue(val);
		// 修改属性后，要写到磁盘才能生效
		OutputFormat format = OutputFormat.createPrettyPrint(); // 漂亮格式：有空格换行
		format.setEncoding("UTF-8");
		FileOutputStream out = new FileOutputStream(filePath);
		XMLWriter writer = new XMLWriter(out, format);
		writer.write(document);
		writer.close();
	}

	/**
	 * @Title: SetViewBoxWithVB   已经有了viewBox，直接设置
	 * @Description: TODO   
	 * @param document
	 * @param val
	 * @param filePath
	 * @throws DocumentException
	 * @throws IOException      
	 * @return: void
	 */
	private static void SetViewBoxWithVB(Document document, String val, String filePath)
			throws DocumentException, IOException {
		Element svgNode = document.getRootElement(); // root -> svg
		svgNode.attribute("viewBox").setValue(val);
		// 修改属性后，要写到磁盘才能生效
		OutputFormat format = OutputFormat.createPrettyPrint(); // 漂亮格式：有空格换行
		format.setEncoding("UTF-8");
		FileOutputStream out = new FileOutputStream(filePath);
		XMLWriter writer = new XMLWriter(out, format);
		writer.write(document);
		writer.close();
	}

	private static int fileCnt = 0;
	private static List<String> nameList = new ArrayList<String>();
	/**
	 * @Title: WriteText   
	 * @Description: TODO   将SVG的text节点分好，写入各个svg。同时可获取svg数目fileCnt和名字nameList
	 * @param document
	 * @throws IOException
	 * @throws DocumentException      
	 * @return: void
	 */
	private static void WriteText(Document document) throws IOException, DocumentException {
		File tmpFile = new File(imagePath + "/resources/template.svg"); // 获取svg模板
		// 拿到<text>节点
		Element svgNode = document.getRootElement(); // root -> svg
		Element g1Node = svgNode.element("g");
		Element g2Node = g1Node.element("g");
		String tmpFilePath = null;
		for (Iterator<Element> it = g2Node.elementIterator(); it.hasNext();) {
			Element childNode = it.next();
			if (childNode.getName() == "text") {
				if (childNode.element("tspan").getTextTrim().equals("Size: Base")) // 文件＋1
				{
					fileCnt++;
					tmpFilePath = imagePath + "/temp_" + fileCnt + ".svg";  // 临时文件
					File target = new File(tmpFilePath);
					try {
						Files.copy(tmpFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
						System.out.println("Copy to file " + fileCnt + " over");
					} catch (IOException e) {
						e.printStackTrace();
					}

				} else if (childNode.element("tspan").getTextTrim().contains("Piece Name")) // 获取文件名
				{
					String name = childNode.element("tspan").getTextTrim();
					if (name.contains(":"))
						nameList.add(childNode.element("tspan").getTextTrim().split(":")[1].trim());
					else {
						System.out.println("分离文件名出错");
						return;
					}
				}
				// 将text写入对应svg文件
				// （尝试用XMLWriter格式化，会使< 变成 &lt
				try {
					FileOutputStream out = new FileOutputStream(tmpFilePath, true);
					OutputStreamWriter osw = new OutputStreamWriter(out);
					osw.write(childNode.asXML());
					osw.close();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @Title: WriteLine   
	 * @Description: TODO   将SVG的line分好写入各个svg
	 * @param document
	 * @throws IOException      
	 * @return: void
	 */
	private static void WriteLine(Document document) throws IOException {
		// line，一图一直线。若后面的line中y1 != y2，那么一定跟前一个line一起
		int fileIdx = 0;
		// 获取<line>节点
		Element svgNode = document.getRootElement(); // root -> svg
		Element g1Node = svgNode.element("g");
		Element g2Node = g1Node.element("g");
		String tmpFilePath = null;
		for (Iterator<Element> it = g2Node.elementIterator(); it.hasNext();) {
			Element childNode = it.next();
			if (childNode.getName() == "line") {
				// 若 y1 ≈ y2，说明属于新文件，fileIdx++
				double y1 = Double.parseDouble(childNode.attributeValue("y1"));
				double y2 = Double.parseDouble(childNode.attributeValue("y2"));
				if (Math.abs(y1 - y2) <= 0.1) {
					fileIdx++;
					tmpFilePath = imagePath + "/temp_" + fileIdx + ".svg";
				}
				// <line>加入对应文档
				FileOutputStream out = new FileOutputStream(tmpFilePath, true);
				OutputStreamWriter osw = new OutputStreamWriter(out);
				osw.write(childNode.asXML());
				osw.close();
				out.close();
			}
		}
	}

	/**
	 * @Title: EndSVG   
	 * @Description: TODO   给每个SVG加上</g></g></svg>使之结构完整
	 * @throws IOException      
	 * @return: void
	 */
	private static void EndSVG() throws IOException {
		String filePath;
		for (int i = 1; i <= fileCnt; i++) {
			filePath = imagePath + "/temp_" + i + ".svg";
			FileOutputStream out = new FileOutputStream(filePath, true);
			OutputStreamWriter osw = new OutputStreamWriter(out);
			osw.write("</g></g></svg>");
			osw.close();
			out.close();
			System.out.println("End file " + i + " over");
		}
	}
	
	private static List<String> viewBoxList = new ArrayList<String>();
	/**
	 * @Title: WritePath   
	 * @Description: TODO   将SVG的path分好写入各个svg，同时获取各自的viewBox
	 * @param document      
	 * @return: void
	 */
	private static void WritePath(Document document) {
		// 通过viewBox判断path是不是属于新图
		// 按照绘图规范，先画外path再画内path
		// 可以顺便把每个文件的viewBox改了，好居中
		int fileIdx = 0;
		// 获取<line>节点
		Element svgNode = document.getRootElement(); // root -> svg
		Element g1Node = svgNode.element("g");
		Element g2Node = g1Node.element("g");
		String tmpFilePath = null;
		String preViewBox = null;
		
		for (Iterator<Element> it = g2Node.elementIterator(); it.hasNext();) {
			Element childNode = it.next();
			if (childNode.getName() == "path") {
				// 情况一，path节点属性d以z结尾（表示闭环），则需要ViewBox判断是否非新图
				// 情况二，path节点属性d无z，则一定不是新图
				if (childNode.attributeValue("d").endsWith(" z")) {
					String viewBox = GetViewBoxFromPath(childNode);
					if (preViewBox == null || (preViewBox != null && !IsSameFile(preViewBox, viewBox))) {
						fileIdx++; // 新图
						tmpFilePath = imagePath + "/temp_" + fileIdx + ".svg";
						// 记录待修改的viewBox
						viewBoxList.add(viewBox);
					}
					preViewBox = viewBox; // 更新preViewBox
				}
				// <path>写入相应svg文档
				try {
					FileOutputStream out = new FileOutputStream(tmpFilePath, true);
					OutputStreamWriter osw = new OutputStreamWriter(out);
					osw.write(childNode.asXML());
					osw.close();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * @Title: IsSameFile   
	 * @Description: TODO   通过viewBox判断，前后Path是否属于同一张图
	 * @param preViewBox
	 * @param viewBox
	 * @return: boolean
	 */
	private static boolean IsSameFile(String preViewBox, String viewBox) {
		// ViewBox格式：minX, maxY, width, height
		double[] data = new double[8];
		String[] temp = preViewBox.concat(" " + viewBox).split("\\s");
		if (temp.length != 8) {
			System.out.println("ViewBox切分出错！");
			return false;
		}
		for (int i = 0; i < data.length; i++)
			data[i] = Double.parseDouble(temp[i]);
		// 如果pre的minX更小，但minX+width更大，就为同一个file
		if (data[0] < data[4] && (data[0] + data[2]) > (data[4] + data[6])) {
//			if(data[1]>data[5] && (data[1]+data[3])>(data[5]+data[6]))
			System.out.println("非新图：" + data[0] + " : " + data[4]);
			return true;
		}
		return false;
	}
	
	/**
	 * @Title: UpdateNameAndViewbox   
	 * @Description: TODO   根据nameList和viewBoxList更新所有svg文件
	 * @throws DocumentException
	 * @throws IOException      
	 * @return: void
	 */
	private static void UpdateNameAndViewbox() throws DocumentException, IOException
	{
		String filePath;
		SAXReader reader = new SAXReader();
		for(int i=1; i<=fileCnt; i++)
		{
			filePath = imagePath + "/temp_" + i + ".svg";
			Document doc = reader.read(filePath);
			String newfilePath;
			File oldF = new File(filePath);
			if (oldF.exists()) {
				newfilePath = filePath.substring(0, filePath.lastIndexOf('/')) + "/" + nameList.get(i-1) + ".svg";
				File newF = new File(newfilePath);
				if(oldF.renameTo(newF)) {
					System.out.println("File "+i+" Rename Success");
				}
				else
					System.out.println("File "+i+" Rename Failure");
			}
			else {
				System.out.println("文件路径错误！");
				return;
			}
			SetViewBoxWithVB(doc, viewBoxList.get(i-1), newfilePath);
		}
	}
}
