package dxfFileProcess;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.batik.transcoder.TranscoderException;
import org.kabeja.dxf.DXFDocument;
import org.xml.sax.SAXException;

public class TestMain {

	public static void main(String[] args) {
		
		// ��ȡ���̸�Ŀ¼/image
		String imagePath = "D:\\Projects\\Project1_Convert\\NewPic";
		// 1��DXF -> SVG��·��������·��/images/��
		//DXFDocument dxfDoc = DXFConvert.GetSourceFile("D:\\Projects\\Project1_Convert\\NewPic/NL-4AB-5PVC.dxf");
		try {
			//DXFConvert.DXF2SVG(dxfDoc, imagePath+"/CQ011312_02.svg");
			SVGConvert.SVG2JPG(imagePath+"/svg/6803103-835.svg", imagePath+"/jpg/6803103-835.jpg", 25.0f);
		} catch (IOException e) {
			e.printStackTrace();
		}   catch (TranscoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
