package dxfFileProcess;

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
	// Ĭ�ϴ��ͼƬ�ļ���ģ���·��
	private static String imagePath;
//	public static void main(String[] args) {
//		SVGDevide(imagePath + "/compPic.svg");
//	}
	/**
	 * @Title: SVGDevide   
	 * @Description: TODO   ��һ����ͼ��SVG��ͼ��Ϊsvg
	 * @param filePath   SVGԴ�ļ�·��
	 * @param outFilePath   ����ļ�·�����޾����ļ���Ĭ�Ϲ���Ŀ¼/images��
	 * @return: void
	 */
	public static void SVGDevide(String filePath, String outFilePath) {
		imagePath = outFilePath;
		// 1.����Reader����
		SAXReader reader = new SAXReader();
		// 2.����xml
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
	 * @Description: TODO   ����path�ڵ������d�����ݣ������viewBox
	 * @param: pathNode
	 * @return: String      
	 * @throws
	 */
	private static String GetViewBoxFromPath(Element pathNode) {
		if (!pathNode.getName().equals("path"))
			return null;
		String posStrs[] = pathNode.attribute("d").getValue().toString().split("\\s");
		// posStrs��ʽ��M -257.9927426804836 -24.15827161102091
		// ǰһ������arrayX����һ������arrayY
		List<Double> listX = new ArrayList<Double>();
		List<Double> listY = new ArrayList<Double>();
		boolean flag = true;
		for (String posStr : posStrs) {
			if (posStr.length() > 1) // �ų�M��L���ַ�
			{
				if (flag)
					listX.add(Double.parseDouble(posStr));
				else
					listY.add(Double.parseDouble(posStr));
				flag = !flag;
			}
		}
		// �޸�<svg>������ViewBox���Ա�֤ͼ�����
		double minX = Collections.min(listX);
		double maxY = Collections.max(listY);
		double width = Math.abs(Collections.max(listX) - minX);
		double height = Math.abs(maxY - Collections.min(listY));
		maxY = -maxY;
		// double[] viewBox = {minX, maxY, width, height};
		// �ɵ���ͼ��λ�á���С
		String viewBox = (minX-1) + " " + (maxY-1) + " " + (width+5) + " " + (height+2);
		return viewBox;
	}

	/**
	 * @Title: SetViewBox   
	 * @Description: TODO  ����������viewBox�ļ������㲢����viewBox
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
		// �޸����Ժ�Ҫд�����̲�����Ч
		OutputFormat format = OutputFormat.createPrettyPrint(); // Ư����ʽ���пո���
		format.setEncoding("UTF-8");
		FileOutputStream out = new FileOutputStream(filePath);
		XMLWriter writer = new XMLWriter(out, format);
		writer.write(document);
		writer.close();
	}

	/**
	 * @Title: SetViewBoxWithVB   �Ѿ�����viewBox��ֱ������
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
		// �޸����Ժ�Ҫд�����̲�����Ч
		OutputFormat format = OutputFormat.createPrettyPrint(); // Ư����ʽ���пո���
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
	 * @Description: TODO   ��SVG��text�ڵ�ֺã�д�����svg��ͬʱ�ɻ�ȡsvg��ĿfileCnt������nameList
	 * @param document
	 * @throws IOException
	 * @throws DocumentException      
	 * @return: void
	 */
	private static void WriteText(Document document) throws IOException, DocumentException {
		File tmpFile = new File(imagePath + "/resources/template.svg"); // ��ȡsvgģ��
		// �õ�<text>�ڵ�
		Element svgNode = document.getRootElement(); // root -> svg
		Element g1Node = svgNode.element("g");
		Element g2Node = g1Node.element("g");
		String tmpFilePath = null;
		for (Iterator<Element> it = g2Node.elementIterator(); it.hasNext();) {
			Element childNode = it.next();
			if (childNode.getName() == "text") {
				if (childNode.element("tspan").getTextTrim().equals("Size: Base")) // �ļ���1
				{
					fileCnt++;
					tmpFilePath = imagePath + "/temp_" + fileCnt + ".svg";  // ��ʱ�ļ�
					File target = new File(tmpFilePath);
					try {
						Files.copy(tmpFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
						System.out.println("Copy to file " + fileCnt + " over");
					} catch (IOException e) {
						e.printStackTrace();
					}

				} else if (childNode.element("tspan").getTextTrim().contains("Piece Name")) // ��ȡ�ļ���
				{
					String name = childNode.element("tspan").getTextTrim();
					if (name.contains(":"))
						nameList.add(childNode.element("tspan").getTextTrim().split(":")[1].trim());
					else {
						System.out.println("�����ļ�������");
						return;
					}
				}
				// ��textд���Ӧsvg�ļ�
				// ��������XMLWriter��ʽ������ʹ< ��� &lt
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
	 * @Description: TODO   ��SVG��line�ֺ�д�����svg
	 * @param document
	 * @throws IOException      
	 * @return: void
	 */
	private static void WriteLine(Document document) throws IOException {
		// line��һͼһֱ�ߡ��������line��y1 != y2����ôһ����ǰһ��lineһ��
		int fileIdx = 0;
		// ��ȡ<line>�ڵ�
		Element svgNode = document.getRootElement(); // root -> svg
		Element g1Node = svgNode.element("g");
		Element g2Node = g1Node.element("g");
		String tmpFilePath = null;
		for (Iterator<Element> it = g2Node.elementIterator(); it.hasNext();) {
			Element childNode = it.next();
			if (childNode.getName() == "line") {
				// �� y1 �� y2��˵���������ļ���fileIdx++
				double y1 = Double.parseDouble(childNode.attributeValue("y1"));
				double y2 = Double.parseDouble(childNode.attributeValue("y2"));
				if (Math.abs(y1 - y2) <= 0.1) {
					fileIdx++;
					tmpFilePath = imagePath + "/temp_" + fileIdx + ".svg";
				}
				// <line>�����Ӧ�ĵ�
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
	 * @Description: TODO   ��ÿ��SVG����</g></g></svg>ʹ֮�ṹ����
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
	 * @Description: TODO   ��SVG��path�ֺ�д�����svg��ͬʱ��ȡ���Ե�viewBox
	 * @param document      
	 * @return: void
	 */
	private static void WritePath(Document document) {
		// ͨ��viewBox�ж�path�ǲ���������ͼ
		// ���ջ�ͼ�淶���Ȼ���path�ٻ���path
		// ����˳���ÿ���ļ���viewBox���ˣ��þ���
		int fileIdx = 0;
		// ��ȡ<line>�ڵ�
		Element svgNode = document.getRootElement(); // root -> svg
		Element g1Node = svgNode.element("g");
		Element g2Node = g1Node.element("g");
		String tmpFilePath = null;
		String preViewBox = null;
		
		for (Iterator<Element> it = g2Node.elementIterator(); it.hasNext();) {
			Element childNode = it.next();
			if (childNode.getName() == "path") {
				// ���һ��path�ڵ�����d��z��β����ʾ�ջ���������ҪViewBox�ж��Ƿ����ͼ
				// �������path�ڵ�����d��z����һ��������ͼ
				if (childNode.attributeValue("d").endsWith(" z")) {
					String viewBox = GetViewBoxFromPath(childNode);
					if (preViewBox == null || (preViewBox != null && !IsSameFile(preViewBox, viewBox))) {
						fileIdx++; // ��ͼ
						tmpFilePath = imagePath + "/temp_" + fileIdx + ".svg";
						// ��¼���޸ĵ�viewBox
						viewBoxList.add(viewBox);
					}
					preViewBox = viewBox; // ����preViewBox
				}
				// <path>д����Ӧsvg�ĵ�
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
	 * @Description: TODO   ͨ��viewBox�жϣ�ǰ��Path�Ƿ�����ͬһ��ͼ
	 * @param preViewBox
	 * @param viewBox
	 * @return: boolean
	 */
	private static boolean IsSameFile(String preViewBox, String viewBox) {
		// ViewBox��ʽ��minX, maxY, width, height
		double[] data = new double[8];
		String[] temp = preViewBox.concat(" " + viewBox).split("\\s");
		if (temp.length != 8) {
			System.out.println("ViewBox�зֳ���");
			return false;
		}
		for (int i = 0; i < data.length; i++)
			data[i] = Double.parseDouble(temp[i]);
		// ���pre��minX��С����minX+width���󣬾�Ϊͬһ��file
		if (data[0] < data[4] && (data[0] + data[2]) > (data[4] + data[6])) {
//			if(data[1]>data[5] && (data[1]+data[3])>(data[5]+data[6]))
			System.out.println("����ͼ��" + data[0] + " : " + data[4]);
			return true;
		}
		return false;
	}
	
	/**
	 * @Title: UpdateNameAndViewbox   
	 * @Description: TODO   ����nameList��viewBoxList��������svg�ļ�
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
				System.out.println("�ļ�·������");
				return;
			}
			SetViewBoxWithVB(doc, viewBoxList.get(i-1), newfilePath);
		}
	}
}
