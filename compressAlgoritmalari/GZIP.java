

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZIP {

	public static void main(String[] args) throws Exception {

		String input = "test";

		System.out.println(compress(input));
		System.out.println(decompress(compress(input)));

	}

	public static String compress(String str) throws Exception {
		if (str == null || str.length() == 0) {
			return null;
		}
		ByteArrayOutputStream obj = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(obj);
		gzip.write(str.getBytes("UTF-8"));
		gzip.close();

		String outString = null;
		for (int i = 0; i < obj.toByteArray().length; i++) {
			outString = outString + obj.toByteArray()[i] + ", ";
		}
		outString = outString.replaceAll("null", "");
		outString = outString.substring(0, outString.length() - 2);
		return outString;

	}

	public static String decompress(String str) throws Exception {
		if (str == null) {
			return null;
		}

		int str_length = str.split(", ").length;
		byte son[] = new byte[str_length];

		for (int i = 0; i < son.length; i++) {
			int swap = Integer.valueOf(str.split(", ")[i]);
			son[i] = (byte) swap;
		}

		GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(son));
		BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
		String outStr = "";
		String line;
		while ((line = bf.readLine()) != null) {
			outStr += line;
		}
		return outStr;
	} 


	static int diziUzunlugu_compressParalel;
	static int threadControlSayac_compressParalel = 0;
	static StringBuilder output_compressParalel_sb = new StringBuilder();

	public static String compressParalel(String str) throws Exception {
		if (str == null || str.length() == 0) {
			return null;
		}

		int bolmeSabiti = 50000; //50000
		diziUzunlugu_compressParalel = str.length() / bolmeSabiti;
		if (str.length() % bolmeSabiti != 0) {
			diziUzunlugu_compressParalel = diziUzunlugu_compressParalel + 1;
		}

		String inputDizi[] = new String[diziUzunlugu_compressParalel];
		String outputDizi[] = new String[diziUzunlugu_compressParalel];
		for (int i = 0; i < diziUzunlugu_compressParalel; i++) {
			if (i + 1 != diziUzunlugu_compressParalel) {
				inputDizi[i] = str.substring(i * bolmeSabiti, (i + 1) * bolmeSabiti);
			} else {
				inputDizi[i] = str.substring(i * bolmeSabiti, ((i * bolmeSabiti) + (str.length() % bolmeSabiti)));
			}
		}

		Thread t1[] = new Thread[diziUzunlugu_compressParalel];
		for (int j = 0; j < t1.length; j++) {
			int val = j;
			t1[val] = new Thread() {
				public void run() {
					ByteArrayOutputStream obj = new ByteArrayOutputStream();
					GZIPOutputStream gzip;
					try {
						gzip = new GZIPOutputStream(obj);
						gzip.write(inputDizi[val].getBytes("UTF-8"));
						gzip.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					String outString = null;
					for (int i = 0; i < obj.toByteArray().length; i++) {
						outString = outString + obj.toByteArray()[i] + ", ";
					}
					outString = outString.replaceAll("null", "");
					outString = outString.substring(0, outString.length() - 2);

					outputDizi[val] = "@" + outString;

				}
			};
			t1[val].start();
		}

		while (true) {
			for (int k = 0; k < diziUzunlugu_compressParalel; k++) {
				if (t1[k] != null && t1[k].isAlive() == false) {

					t1[k] = null;
					System.gc();
					threadControlSayac_compressParalel = threadControlSayac_compressParalel + 1;

				}
			}
			if (threadControlSayac_compressParalel == diziUzunlugu_compressParalel) {

				for (int l = 0; l < t1.length; l++) { 
					output_compressParalel_sb.append(outputDizi[l]);
				} 
 
				break;
			}
		}

		return output_compressParalel_sb.toString();

	}

	static int diziUzunlugu_decompressParalel;
	static int threadControlSayac_decompressParalel = 0;
	static StringBuilder output_decompressParalel_sb = new StringBuilder();

	public static String decompressParalel(String str) throws Exception {
		if (str == null) {
			return null;
		}
  
		str = str.substring(1, str.length());
		diziUzunlugu_decompressParalel = str.split("@").length;
		String inputDizi[] = new String[diziUzunlugu_decompressParalel];
		String outputDizi[] = new String[diziUzunlugu_decompressParalel];

		for (int i = 0; i < diziUzunlugu_decompressParalel; i++) {
			inputDizi[i] = str.split("@")[i];
		}
		Thread t1[] = new Thread[diziUzunlugu_decompressParalel];
		for (int j = 0; j < t1.length; j++) {
			int val = j;
			t1[val] = new Thread() {
				public void run() {
					int str_length = inputDizi[val].split(", ").length;
					byte son[] = new byte[str_length];

					for (int i = 0; i < son.length; i++) {
						int swap = Integer.valueOf(inputDizi[val].split(", ")[i].replaceAll("null", ""));
						son[i] = (byte) swap;
					}
					GZIPInputStream gis;
					try {

						gis = new GZIPInputStream(new ByteArrayInputStream(son));
						BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
						String outStr = "";
						String line;

						while ((line = bf.readLine()) != null) {
							outStr += line;
						}
						outputDizi[val] = outStr;
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			};
			t1[val].start();

		}

		while (true) {
			for (int k = 0; k < diziUzunlugu_decompressParalel; k++) {
				if (t1[k] != null && t1[k].isAlive() == false) {
					t1[k] = null;
					System.gc();
					threadControlSayac_decompressParalel = threadControlSayac_decompressParalel + 1;

				}
			}
			if (threadControlSayac_decompressParalel == diziUzunlugu_decompressParalel) {
				for (int l = 0; l < t1.length; l++) {
					output_decompressParalel_sb.append(  outputDizi[l]);
				}
				break;
			}
		}
		return output_decompressParalel_sb.toString();
	}

}