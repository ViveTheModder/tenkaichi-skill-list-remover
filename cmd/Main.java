package cmd;
//Tenkaichi Skill List Remover by ViveTheModder
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

import gui.App;

public class Main 
{
	private static boolean bt2Mode, isSklLstPresent;
	public static boolean wiiMode;
	public static int fileCnt=0;
	public static int[] sklLstIds = {-1,-1,-1,-1,-1,-1,-1,-1,-1};
	public static final String[] BT2_SKL_LST_LANGS = {"German","French","Italian","Japanese","Korean (Unused)","Spanish","British English","American English"};
	public static final String[] BT3_SKL_LST_LANGS = 
	{"Japanese","American English","Spanish (Unused)","British English","Spanish","German","French","Italian","Korean (Unused)"};
	
	private static boolean isCharaCostumePak(File pakRef) throws IOException
	{
		wiiMode=false; //treat every PAK as if it is in Little Endian
		RandomAccessFile pak = new RandomAccessFile(pakRef,"r");
		int numPakContents = LittleEndian.getInt(pak.readInt());
		if (numPakContents<0) //prevent negative seek offset
		{
			wiiMode=true;
			numPakContents = LittleEndian.getInt(pak.readInt());
		}
		if (numPakContents==252) bt2Mode=false;
		else if (numPakContents==250) bt2Mode=true;
		pak.seek((numPakContents+1)*4);
		int fileSize = LittleEndian.getInt(pak.readInt());
		int actualFileSize = (int) pak.length();
		pak.close();
		if (fileSize==actualFileSize && numPakContents>=250 && numPakContents<=252) return true;
		return false;
	}
	private static void delSklLst(File pakRef, int id) throws IOException
	{
		RandomAccessFile pak = new RandomAccessFile(pakRef,"rw");
		int numSizes = LittleEndian.getInt(pak.readInt());
		int[] positions = new int[numSizes+1];
		int[] sizes = new int[numSizes];
		int tempPos=0;
		
		for (int i=1; i<=numSizes; i++) //initialize positions and sizes
		{
			if (tempPos==0) positions[i-1] = LittleEndian.getInt(pak.readInt()); //current position
			else positions[i-1] = tempPos;
			positions[i] = LittleEndian.getInt(pak.readInt()); //next position
			tempPos = positions[i]; //temporary position which exists to prevent reading the same positions twice
			sizes[i-1] = positions[i]-positions[i-1];
		}
		if (sizes[id]>0)
		{
			isSklLstPresent=true;
			//fix index
			pak.seek((id+2)*4);
			for (int i=id+1; i<=numSizes; i++) pak.writeInt(LittleEndian.getInt(positions[i]-sizes[id]));
			pak.seek(positions[id+1]);
			int fullFileSize = (int)pak.length();
			int restOfFileSize = fullFileSize - positions[id+1];
			byte[] restOfFile = new byte[restOfFileSize];
			pak.read(restOfFile); //copy the rest of the file contents before overwriting
			pak.seek(positions[id]);
			//actual overwriting process
			pak.write(restOfFile);
			pak.setLength(fullFileSize-sizes[id]);
			pak.close();
		}
		else 
		{
			isSklLstPresent=false;
			System.out.println("Nevermind, no such Skill List is present anyway.");
		}
	}
	private static void deleteInputSkillLists(File pakRef) throws IOException
	{
		int sklLstIdStart=0;
		if (isCharaCostumePak(pakRef))
		{
			if (bt2Mode) sklLstIdStart=40;
			else sklLstIdStart=43;
			for (int i=0; i<sklLstIds.length; i++)
			{
				if (sklLstIds[i]!=-1)
				{
					//this if condition assures the IDs are only incremented once... I hope...
					if (sklLstIds[i]<sklLstIdStart) sklLstIds[i]+=sklLstIdStart;
					
					String currSklLstLang="";
					if (bt2Mode) currSklLstLang = BT2_SKL_LST_LANGS[i];
					else currSklLstLang = BT3_SKL_LST_LANGS[i];
					if (isSklLstPresent) System.out.println("Removing "+currSklLstLang+" Skill List from "+pakRef.getName()+"...");
					delSklLst(pakRef,sklLstIds[i]);
				}
			}
		}
	}
	public static void traverse(File src) throws IOException
	{
		if (src.isDirectory())
		{
			File[] dirs = src.listFiles();
			if (dirs!=null)
			{
				for (File newSrc: dirs) traverse(newSrc);
			}
		}
		else if (src.isFile()) 
		{
			String nameLower = src.getName().toLowerCase();
			if (nameLower.endsWith("pak") && !(nameLower.contains("anm") || nameLower.contains("eff") || nameLower.contains("voice")))
			{
				fileCnt++;
				if (App.fileLabel!=null) 
				{
					App.fileLabel.setText(src.getCanonicalPath());
					App.fileCntLabel.setText("Overwritten Costumes: "+fileCnt);
				}
				deleteInputSkillLists(src);
			}
		}
	}
	public static void main(String[] args) 
	{
		try 
		{
			if (args.length>0)
			{
				if (args[0].equals("-c"))
				{
					File src=null;
					Scanner sc = new Scanner(System.in);
					while (src==null)
					{
						System.out.println("Enter a valid path to a folder containing character costume files.\nSubfolders will also be searched for such files.");
						String path = sc.nextLine();
						File temp = new File(path);
						if (temp.isDirectory()) src=temp;
					}
					while (sklLstIds[0]==-1)
					{
						System.out.println("Enter a series of numbers (up to 8 for BT2 or 9 for BT3, separated by commas)\nrepresenting the Skill Lists that will be removed:");
						System.out.println("[Budokai Tenkaichi 2 Skill Lists]");
						for (int i=0; i<BT2_SKL_LST_LANGS.length; i++) System.out.println(i+". "+BT2_SKL_LST_LANGS[i]);
						System.out.println("[Budokai Tenkaichi 3 Skill Lists]");
						for (int i=0; i<BT3_SKL_LST_LANGS.length; i++) System.out.println(i+". "+BT3_SKL_LST_LANGS[i]);
						String input = sc.nextLine();
						input = input.replace(" ", ""); //remove spaces just in case
						String[] inputArr = input.split(",");
						for (int i=0; i<inputArr.length; i++)
						{
							if (inputArr[i].matches("[0-8]+") && inputArr[i].length()==1)
								sklLstIds[i]=Integer.parseInt(inputArr[i]);
						}
					}
					sc.close();
					long start = System.currentTimeMillis();
					traverse(src);
					long finish = System.currentTimeMillis();
					double time = (finish-start)/1000.0;
					System.out.println("Time: "+time+" s");
				}
			}
			else gui.App.main(args);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}