
import java.util.zip.*;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import java.util.zip.*;
import java.io.UnsupportedEncodingException;

import java.io.UnsupportedEncodingException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import fotorenkKilit.LZW;

public class DEFLATE {

	public static String compress(String str) {
        str=str.replaceAll("@", "0000000000000000").replaceAll("GZIP", "");
		int str_length = str.split(", ").length;
		byte son[] = new byte[str_length];

		for (int i = 0; i < son.length; i++) {
			int swap = Integer.valueOf(str.split(", ")[i]);
			son[i] = (byte) swap;
		}

		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final byte[] buf = new byte[999999999];
		final Deflater compresser = new Deflater(Deflater.BEST_COMPRESSION);
		compresser.setInput(son);
		compresser.finish();
		while (!compresser.finished()) {
			final int compressedDataLength = compresser.deflate(buf);
			bos.write(buf, 0, compressedDataLength);
		}
		compresser.end();

		String outString = null;
		for (int i = 0; i < bos.toByteArray().length; i++) {
			outString = outString + bos.toByteArray()[i] + ", ";
		}
		outString = outString.replaceAll("null", "");
		outString = outString.substring(0, outString.length() - 2);
		// outString = outString.replaceAll(", ", ",");
		return outString;
	}

	public static String decompress(String str) throws DataFormatException {
		str = str.replaceAll("  ", " ");
		
		int str_length = str.split(", ").length;
		byte son[] = new byte[str_length];

		for (int i = 0; i < son.length; i++) {
			int swap = Integer.valueOf(str.split(", ")[i]);
			son[i] = (byte) swap;
		}

		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final byte[] buf = new byte[1024000];
		final Inflater decompresser = new Inflater();
		decompresser.setInput(son, 0, son.length);
		while (!decompresser.finished()) {
			final int resultLength = decompresser.inflate(buf);
			bos.write(buf, 0, resultLength);
		}
		decompresser.end();

		String outString = null;
		for (int i = 0; i < bos.toByteArray().length; i++) {
			outString = outString + bos.toByteArray()[i] + ", ";
		}
		outString = outString.replaceAll("null", "");
		outString = outString.substring(0, outString.length() - 2);
		
		outString=outString.replaceAll("0000000000000000", "@") ;
		outString=outString+"GZIP";

		return outString;

	}

	static int diziUzunlugu_compressParalel;
	static int threadControlSayac_compressParalel = 0;
	static String output_compressParalel = new String();

	public static String compressParalel(String str) {
		int bolmeSabiti = 10002; // 500000
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
					System.out.println("thread girdi " + val + " .." + inputDizi[val]);

					int str_length = inputDizi[val].split(", ").length;
					byte son[] = new byte[str_length];
					for (int i = 0; i < son.length; i++) {
						int swap = Integer.valueOf(inputDizi[val].split(", ")[i]);
						son[i] = (byte) swap;
					}

					final ByteArrayOutputStream bos = new ByteArrayOutputStream();
					final byte[] buf = new byte[999999999];
					final Deflater compresser = new Deflater(Deflater.BEST_COMPRESSION);
					compresser.setInput(son);
					compresser.finish();
					while (!compresser.finished()) {
						final int compressedDataLength = compresser.deflate(buf);
						bos.write(buf, 0, compressedDataLength);
					}
					compresser.end();

					String outString = null;
					for (int i = 0; i < bos.toByteArray().length; i++) {
						outString = outString + bos.toByteArray()[i] + ", ";
					}
					outString = outString.replaceAll("null", "");
					outString = outString.substring(0, outString.length() - 2);

					outputDizi[val] = "_" + outString;
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
					System.out.println("outputDizi[l] " + l + " .." + outputDizi[l]);
					output_compressParalel = output_compressParalel + outputDizi[l];
				}
				break;
			}
		}

		return output_compressParalel;

	}

	static int diziUzunlugu_decompressParalel;
	static int threadControlSayac_decompressParalel = 0;
	static String output_decompressParalel = new String();

	public static String decompressParalel(String str) throws DataFormatException {
		str = str.substring(1, str.length());

		diziUzunlugu_decompressParalel = str.split("_").length;
		String inputDizi[] = new String[diziUzunlugu_decompressParalel];
		String outputDizi[] = new String[diziUzunlugu_decompressParalel];

		for (int i = 0; i < diziUzunlugu_decompressParalel; i++) {
			inputDizi[i] = str.split("_")[i];
		}
		Thread t1[] = new Thread[diziUzunlugu_decompressParalel];
		for (int j = 0; j < t1.length; j++) {
			int val = j;
			t1[val] = new Thread() {
				public void run() {

					int str_length = inputDizi[val].split(", ").length;
					byte son[] = new byte[str_length];

					for (int i = 0; i < son.length; i++) {
						int swap = Integer.valueOf(inputDizi[val].split(", ")[i]);
						son[i] = (byte) swap;
					}

					final ByteArrayOutputStream bos = new ByteArrayOutputStream();
					final byte[] buf = new byte[1024000];
					final Inflater decompresser = new Inflater();
					decompresser.setInput(son, 0, son.length);
					while (!decompresser.finished()) {
						int resultLength = 0;
						try {
							resultLength = decompresser.inflate(buf);
						} catch (DataFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						bos.write(buf, 0, resultLength);
					}
					decompresser.end();

					String outString = null;
					for (int i = 0; i < bos.toByteArray().length; i++) {
						outString = outString + bos.toByteArray()[i] + ", ";
					}
					outString = outString.replaceAll("null", "");
					outString = outString.substring(0, outString.length() - 2);
					outputDizi[val] = outString;

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
					output_decompressParalel = output_decompressParalel + outputDizi[l];
				}
				break;
			}
		}
		return output_decompressParalel;

	}


}
