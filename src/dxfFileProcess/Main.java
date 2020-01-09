package dxfFileProcess;

import java.io.FileNotFoundException;

import org.kabeja.dxf.DXFDocument;
import org.xml.sax.SAXException;

/*
 * 1��DXF -> SVG (Kabeja)
 * 2��SVG -> svgs 
 * 			���д���viewBox="����"����"���SVG��ͼ�����ɿո�򶺺ŷָ���4��ֵ��(min x, min y, width, height)"
 * 				����ViewBox��(��Сx,  |���y|�� ���x-��Сx�����y-��Сy)
 * 			��ȡxml�м�¼��Piece Name
 * 			(���ϴ�������DOM4J)
 * 3��svg -> pdf (batik),  pdf -> jpg (pdfbox)
 */
public class Main {

	public static void main(String[] args) {
		// ��ȡ���̸�Ŀ¼/image
		String imagePath = System.getProperties().getProperty("user.dir")+"/images";
		// 1��DXF -> SVG��·��������·��/images/��
		DXFDocument dxfDoc = DXFConvert.GetSourceFile(imagePath+"/resources/compPic.dxf");
		try {
			DXFConvert.DXF2SVG(dxfDoc, imagePath+"/resources/compPic.svg");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		// 2��SVG -> svgs
		SVGProcess.SVGDevide(imagePath+"/resources/compPic.svg", imagePath);
		// 3��svgs -> jpgs
		SVGProcess.SVG2JPG();
		System.out.println("ת����ɣ�");
	}

}
