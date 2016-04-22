** Instructions to import java-json.jar file to Eclipse
  *
  * A.) Download the ZIP file from this URL and extract it to get the Jar.
  *     http://www.java2s.com/Code/JarDownload/java/java-json.jar.zip
  * B.) Add the Jar to your build path. 
  *	   To Add this Jar to your build path, follow these steps:
  *     1.) Right click the Project > 
  *     2.)				Build Path > 
  *     3.)	   Configure build path>
  *     4.) 	  Select Libraries tab >
  *	    5.) Click Add External Libraries >
  *     6.) Select the Jar file Download
  * C.) Update String loc_super_block (on line 30) --> location of super block on your laptop
  * D.) Compile and run the code
  * E.) Comments have been provided throughout the code for a better understanding.
  *     
  *     Name: Vaibhav Aggarwal
  *     NET ID: va771
 **/

import java.io.*;
import java.time.Instant;
import java.util.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.*;
import java.util.concurrent.ThreadLocalRandom;

public class Vaibhav {

	static String loc_super_block = "./FS/fusedata.0";          //Storing location of super block
														
	static String fusedata_loc = loc_super_block.replace("0", "");           //Storing location of all other files in FS
	int max_num_blocks = 10000;                                // Maximum number of free blocks
	int BLOCK_SIZE = 4096;                                     // given block size
	int dev_id = 20;                                           // Given device ID of this FileSystem
	int free_blocks = 400;                                     // Number of free blocks per location
	File super_block = new File(loc_super_block);
	String content, content_copy, current_content, current_content_copy;
	FileReader read;
	List<Integer> store_actual_free_blocks = new ArrayList<>();		//ArrayList to store actual free blocks
	List<Integer> store_current_free_blocks = new ArrayList<>();	//ArrayList to store current free blocks	
	static long unix = Instant.now().getEpochSecond(); 				// Calculating and storing present UNIX time

	public static void main(String[] args) throws IOException, JSONException {

		Vaibhav V = new Vaibhav();
		String root = fusedata_loc + Integer.toString(V.getValue("root", loc_super_block));		//Storing location of root directory
		int root_loc = V.getValue("root", loc_super_block);
		V.check_dev_id();											//function to check Device ID
		V.check_super_time(unix);									//Function to check time for super block
		V.check_time(unix, root, "d");								//Function to check time for all directories and files
		V.create_free_block_array();								//Function to store actual free blocks in ArrayList
		V.store_free_blocks();										//Function to store current free blocks in ArrayList
		V.find_and_write_missing_free_blocks();						//Function to find and write missing free blocks
		V.directory(root_loc, 0, "d");								//Function to check . and ..
		V.indirect(root_loc, 0, "d");								//Function to check indirect value
		V.sizeCheck(root_loc, 0, "d");								//Function to check size

	}
	
	
	/* function which returns value of the attributes in specified location */

	@SuppressWarnings("finally")
	public int getValue(String keys, String loc) throws IOException, JSONException {
		try {
			read = new FileReader(loc);
			File requestedFile = new File(loc);
			char[] chars = new char[(int) requestedFile.length()];
			read.read(chars);
			content = new String(chars);
			content_copy = content;
			read.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			JSONObject json = new JSONObject(content);						//Using JSON object
			return json.getInt(keys);										//Returns value of key
		}
	}

	/* Checking Device ID of super block */

	public void check_dev_id() throws IOException, JSONException {

		if (getValue("devId", loc_super_block) == dev_id) {
			System.out.println("Dev id (SUPER) --> correct");
		} else {
			System.out.println("Dev id (SUPER) --> not correct");			//if Device ID not correct, stop the program
			System.exit(1);
		}
	}

	/* Checking creation time of Super Block */

	public void check_super_time(long time) throws IOException, JSONException {

		if (getValue("creationTime", loc_super_block) < time) {
			System.out.println("Creation time (SUPER) --> past");
		} else {
			System.out.println("Creation time (SUPER) --> not in past");
			content_copy = content_copy.replace("\"creationTime\":" + getValue("creationTime", loc_super_block),
					"\"creationTime\":" + time);
			PrintWriter pw = new PrintWriter(loc_super_block, "UTF-8");
			pw.println(content_copy);
			pw.close();
			System.out.println("Creation Time (SUPER) --> updated to Present Unix time: " + time);
		}
	}

	/* Checking atime, mtime, ctime of all the locations */

	public void check_time(long time, String loc, String type) throws IOException, JSONException {

		// System.out.println(rootFileLoc);
		BufferedReader br1 = new BufferedReader(new FileReader(loc));
		String fileContent = br1.readLine();
		br1.close();
		String fileContentCopy = fileContent;
		JSONObject time_json = new JSONObject(fileContent);
		long atime = time_json.getLong("atime");
		long ctime = time_json.getLong("ctime");
		long mtime = time_json.getLong("mtime");
		// System.out.println("root
		// time"+root_atime+","+root_ctime+","+root_mtime);
		System.out.println("\n-------------" + loc + " TIME Details-------------\n");
		if (atime < time) {
			System.out.println("Access time --> past");
		} else {
			System.out.println("Access time --> not in past");
			fileContent = fileContent.replace("atime\":" + Long.toString(atime), "atime\":" + time);		//if atime not in past, then
			PrintWriter pw = new PrintWriter(fileContent, "UTF-8");											//replace it by present time
			pw.println(fileContent);
			pw.close();
			System.out.println("Access Time --> updated to Present Unix time: " + time);
		}
		if (ctime < time) {
			System.out.println("Creation time --> past");
		} else {
			System.out.println("Creation time --> not in past");
			fileContent = fileContent.replace("ctime\":" + Long.toString(ctime), "ctime\":" + time);		//if ctime not in past, then
			PrintWriter pw = new PrintWriter(fileContent, "UTF-8");											//replace it by present time
			pw.println(fileContent);
			pw.close();
			System.out.println("Creation Time  --> updated to Present Unix time: " + time);
		}
		if (mtime < time) {
			System.out.println("Modification time --> past");
		} else {
			System.out.println("Modification time --> not in past");
			fileContent = fileContent.replace("mtime\":" + Long.toString(mtime), "mtime\":" + time);		//if mtime not in past, then
			PrintWriter pw = new PrintWriter(fileContent, "UTF-8");											//replace it by present time
			pw.println(fileContent);
			pw.close();
			System.out.println("Modification Time --> updated to Present Unix time: " + time);
		}

		if (type.equals("d")) {

			JSONObject inode_json = new JSONObject(fileContentCopy);
			JSONArray array = inode_json.getJSONArray("filename_to_inode_dict");				//Creating json array for inode
			for (int i = 0; i < array.length(); i++) {											//Loop from 0 to number of inode files
				String inode_type = array.getJSONObject(i).getString("type");
				String inode_name = array.getJSONObject(i).getString("name");

				if (inode_type.equals("f")) {							//if type=f,check time for its location
					String inode_loc = fusedata_loc + Integer.toString(array.getJSONObject(i).getInt("location"));
					check_time(unix, inode_loc, inode_type);
				}
				if (inode_type.equals("d") && !inode_name.equals(".") && !inode_name.equals("..")) {	//if type=d,check time for its location
					String inode_loc = fusedata_loc + Integer.toString(array.getJSONObject(i).getInt("location"));
					check_time(unix, inode_loc, inode_type);
				}
				if (!inode_type.equals("d") && !inode_type.equals("f")) {
					System.out.println(inode_name+" in "+loc+" has incorrect name type");
				}
			}
		}
		if (!type.equals("d") && !type.equals("f")) {
			System.out.println(loc+" incorrect type");
		}
	}

	/* creating an arrayList of all free blocks */

	public void create_free_block_array() throws JSONException, IOException {
		for (int i = 0; i < getValue("maxBlocks", loc_super_block); i++) {
			File current = new File(fusedata_loc + i);
			if (current.exists()) {						//checking if file exists
				continue;
			} else {
				store_actual_free_blocks.add(i);		//if file does not exist, then its a free block, hence add it to ArrayList
			}
		}
		System.out.println("\nFree block array list created");
	}

	/* Creating an arrayList of all the actual free blocks */

	public void store_free_blocks() throws IOException, JSONException {
		for (int i = getValue("freeStart", loc_super_block); i <= getValue("freeEnd", loc_super_block); i++) {
			try {
				File current_file = new File(fusedata_loc + i);
				read = new FileReader(current_file);
				char[] chars = new char[(int) current_file.length()];
				read.read(chars);
				current_content = new String(chars);
				current_content_copy = current_content;
				read.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (read != null) {
					read.close();
				}
				current_content = current_content.replace("[", "");			//Removing square brackets
				current_content = current_content.replace("]", "");			//Removing square brackets
				for (int k = 0; k < current_content.split(",").length; k++) {
					File f = new File(fusedata_loc+current_content.split(",")[k].toString());
					if(!f.exists()){										// If free block does not exist, then add to list
						store_current_free_blocks.add(Integer.parseInt(current_content.split(",")[k]));
					}else{													//if free block exist, then remove it from current file
						current_content_copy = current_content_copy.replace("["+current_content.split(",")[k]+",","[" );
						current_content_copy = current_content_copy.replace(","+current_content.split(",")[k]+",","," );
						current_content_copy = current_content_copy.replace(","+current_content.split(",")[k]+"]","]" );
						PrintWriter writer = new PrintWriter(fusedata_loc+Integer.toString(i), "UTF-8");
						writer.print(current_content_copy);
						writer.close();
					}																					
				}																						
			}
		}	
		System.out.println("Current free blocks stored in list");
	}

	/* Comparing both arrayList, and deleting common numbers from store_actual_free_blocks
	 * Remaining elements in store_actual_free_blocks will be the missing free blocks
	 * Then divide the file location by 400, and add it to the result file
	 */

	public void find_and_write_missing_free_blocks() throws IOException {
		List<Integer> missing_free_blocks = new ArrayList<>(store_actual_free_blocks);
		missing_free_blocks.removeAll(store_current_free_blocks);
		if (missing_free_blocks.size() == 0)										//If no missing free blocks found, then print this
			System.out.println("No missing free blocks\n");
		else {
			System.out.println("Missing free blocks --> Written in");
			for (int l = 0; l < missing_free_blocks.size(); l++) {					//Looping till total number of missing free blocks
				
				int block_loc = missing_free_blocks.get(l) / free_blocks + 1; 		//location = number / 400 + 1
				String location = fusedata_loc + block_loc;							//Extract the complete location of file
				File allocate = new File(location);
				FileReader readTemp = null;
				String contentTemp = null;
				try {
					readTemp = new FileReader(allocate);
					char[] chars = new char[(int) allocate.length()];
					readTemp.read(chars);
					contentTemp = new String(chars);					//Entire content of file in this variable
					contentTemp = contentTemp.replace("]", "");			//removing the square bracket from the end of file content
					readTemp.close();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (readTemp != null) {
						readTemp.close();
					}
				}
				contentTemp = contentTemp + "," + missing_free_blocks.get(l) + "]";		//Writing the missing free block at end of file
				PrintWriter writer = new PrintWriter(allocate, "UTF-8");
				writer.print(contentTemp);
				System.out.println(missing_free_blocks.get(l) + " --> fusedata." + block_loc);
				writer.close();
			}
		}
	}

	/*Checking if . and .. exist and checking their location values */

	public void directory(int loc, int parent, String type) throws IOException, JSONException {

		if (type.equals("d")) {
			File location = new File(fusedata_loc + Integer.toString(loc));
			BufferedReader br1 = new BufferedReader(new FileReader(location));
			String dirContents = br1.readLine();
			br1.close();
			JSONObject old_inode_json = new JSONObject(dirContents);
			JSONArray array = old_inode_json.getJSONArray("filename_to_inode_dict");
			int flag1 = 0, flag2 = 0;								//Initializing flag1 for . and flag2 for ..
			for (int i = 0; i < array.length(); i++) {
				String inode_types = array.getJSONObject(i).getString("type");
				String inode_names = array.getJSONObject(i).getString("name");
				if (inode_types.equals("d") && inode_names.equals(".")) {		//if . found, set flag1 = 1
					flag1 = 1;
				}
				if (inode_types.equals("d") && inode_names.equals("..")) {		//if .. found, set flag1 = 1
					flag2 = 1;
				}
			}
			if (flag1 == 0) {						//if . not found, then write it back to directory
				System.out.println("d . file --> fusedata." + loc + " --> missing");
				
				/* here, instead of writing the correct location of name =".", I'm writing 0 as the default location
				 * After writing type="d",name=".",location=0, the program goes to the location checker where the location is corrected */
				
				dirContents = dirContents.replace("[",
						"[{\"type\":\"d\",\"name\":\".\",\"location\":" + Integer.parseInt("0") + "},");
				PrintWriter pw = new PrintWriter(fusedata_loc + Integer.toString(loc), "UTF-8");
				pw.println(dirContents);
				pw.close();
				System.out.println("d . file --> fusedata." + loc + " --> added");
			}
			if (flag2 == 0) {
				System.out.println("d .. file --> fusedata." + loc + " --> missing");
				
				/* here, instead of writing the correct location of name ="..", I'm writing 0 as the default location
				 * After writing type="d",name="..",location=0, the program goes to the location checker where the location is corrected */
				
				dirContents = dirContents.replace("[",
						"[{\"type\":\"d\",\"name\":\"..\",\"location\":" + Integer.parseInt("0") + "},");
				PrintWriter pw = new PrintWriter(fusedata_loc + Integer.toString(loc), "UTF-8");
				pw.println(dirContents);
				pw.close();
				System.out.println("d .. file --> fusedata." + loc + " --> added");
			}
			
			BufferedReader br2 = new BufferedReader(new FileReader(location));
			String dirContent = br2.readLine();
			br2.close();
			JSONObject inode_json = new JSONObject(dirContent);
			JSONArray new_array = inode_json.getJSONArray("filename_to_inode_dict");

			for (int i = 0; i < new_array.length(); i++) {				//Looping till total number of f's and d's in inode
				String inode_type = new_array.getJSONObject(i).getString("type");
				String inode_name = new_array.getJSONObject(i).getString("name");
				int inode_location = new_array.getJSONObject(i).getInt("location");

				if (inode_type.equals("f")) {			//if type = f, then move on ahead
					continue;
				} 
				
				/* when type=d and name =. , we have 2 cases:
				 * 		1.) when current location = self location
				 * 		2.) when current location != self location
				 * */
				
				else if (inode_type.equals("d") && inode_name.equals(".") && inode_location == loc) {
					System.out.println("Location d. --> fusedata." + loc + " --> correct");
				} else if (inode_type.equals("d") && inode_name.equals(".") && inode_location != loc) {

					System.out.println("Location d. --> fusedata." + loc + " --> wrong");
					dirContent = dirContent.replace("\"name\":\".\",\"location\":" + inode_location,
							"\"name\":\".\",\"location\":" + loc);
					PrintWriter pw1 = new PrintWriter(fusedata_loc + Integer.toString(loc), "UTF-8");
					pw1.println(dirContent);
					pw1.close();
					System.out.println("Location d. --> fusedata." + loc + " --> updated to " + loc);
				}
				
				/* * when type=d and name =.. , we have 5 cases:
				 * 		1.) when its ROOT directory & when current location = root location --> correct
				 * 		2.) when its ROOT directory & when current location != root location --> wrong
				 * 		3.) when its ANY OTHER directory & current location = parent location --> correct
				 * 		4.) when its ANY OTHER directory & current location != parent location --> wrong
				 * 		5.) when its ANY OTHER directory & name != "." & name != ".." --> call directory() function
				 * */
		
				else if (inode_type.equals("d") && inode_name.equals("..") && loc == getValue("root", loc_super_block)
						&& inode_location == getValue("root", loc_super_block)) {
					System.out.println("Location d.. --> fusedata." + loc + " --> correct");
				} else if (inode_type.equals("d") && inode_name.equals("..") && loc == getValue("root", loc_super_block)
						&& inode_location != getValue("root", loc_super_block)) {

					System.out.println("Location d.. --> fusedata." + loc + " --> wrong");
					dirContent = dirContent.replace("\"name\":\"..\",\"location\":" + inode_location,
							"\"name\":\"..\",\"location\":" + loc);
					PrintWriter pw = new PrintWriter(fusedata_loc + Integer.toString(loc), "UTF-8");
					pw.println(dirContent);
					pw.close();
					System.out.println("Location d.. --> fusedata." + loc + " --> updated to " + loc);
				} else if (inode_type.equals("d") && inode_name.equals("..") && loc != getValue("root", loc_super_block)
						&& inode_location == parent) {
					System.out.println("Location d.. --> fusedata." + loc + " --> correct");
				} else if (inode_type.equals("d") && inode_name.equals("..") && loc != getValue("root", loc_super_block)
						&& inode_location != parent) {

					System.out.println("Location d.. --> fusedata." + loc + " --> wrong");
					dirContent = dirContent.replace("\"name\":\"..\",\"location\":" + inode_location,
							"\"name\":\"..\",\"location\":" + parent);
					PrintWriter pw = new PrintWriter(fusedata_loc + Integer.toString(loc), "UTF-8");
					pw.println(dirContent);
					pw.close();
					System.out.println("Location d.. --> fusedata." + loc + " --> updated to " + parent);

				} else if (inode_type.equals("d") && !inode_name.equals(".") && !inode_name.equals("..")) {
					directory(inode_location, loc, inode_type);
				}
			}
		}
	}

	/* Checking indirect */

	public void indirect(int loc, int parent, String type) throws IOException, JSONException {
		File location = new File(fusedata_loc + Integer.toString(loc));
		BufferedReader br1 = new BufferedReader(new FileReader(location));
		String dirContent = br1.readLine();
		br1.close();
		JSONObject indirect_json = new JSONObject(dirContent);
		JSONArray array = indirect_json.getJSONArray("filename_to_inode_dict");
		List<Integer> file_loc = new ArrayList<Integer>();
		for (int i = 0; i < array.length(); i++) {
			String inode_type = array.getJSONObject(i).getString("type");
			String inode_name = array.getJSONObject(i).getString("name");
			int inode_location = array.getJSONObject(i).getInt("location");
			if (inode_type.equals("f") && !inode_name.equals(null)) {
				file_loc.add(inode_location);
			} else if (inode_type.equals("d") && !inode_name.equals(".") && !inode_name.equals("..")) {
				indirect(inode_location, loc, inode_type);
			}
		}
		for (int j = 0; j < file_loc.size(); j++) {
			File Fpath = new File(fusedata_loc + Integer.toString(file_loc.get(j)));
			BufferedReader br = new BufferedReader(new FileReader(Fpath));
			String fileContent = br.readLine();
			br.close();
			JSONObject file_json = new JSONObject(fileContent);
			int file_indirect = file_json.getInt("indirect");
			int file_loc_point = file_json.getInt("location");
			int array_length = getArrayCount(file_loc_point);
			if (file_indirect == 0 && array_length != 0) {
				int new_file_indirect = 1;
				fileContent = fileContent.replace("\"indirect\":" + file_indirect, "\"indirect\":" + new_file_indirect);
				PrintWriter pw1 = new PrintWriter(Fpath, "UTF-8");
				pw1.println(fileContent);
				pw1.close();
				System.out.println("Indirect --> fusedata." + file_loc.get(j) + " --> updated to " + new_file_indirect);
			} else if (file_indirect != 0 && array_length == 0) {
				int new_file_indirect = 0;
				fileContent = fileContent.replace("\"indirect\":" + file_indirect, "\"indirect\":" + new_file_indirect);
				PrintWriter pw1 = new PrintWriter(Fpath, "UTF-8");
				pw1.println(fileContent);
				pw1.close();
				System.out.println("Indirect --> fusedata." + file_loc.get(j) + " --> updated to " + new_file_indirect);
			} else {
				System.out.println("Indirect --> fusedata." + file_loc.get(j) + " --> correct");
			}
		}
	}

	/* Finding the number of elements in the array at given location */

	public int getArrayCount(int loc) throws IOException {
		File path = new File(fusedata_loc + Integer.toString(loc));
		BufferedReader br = new BufferedReader(new FileReader(path));
		String arrayFileContent = br.readLine();
		br.close();
		int arrayLength = 0;
		if (arrayFileContent == null) {
			arrayLength = 0;
		} else if (arrayFileContent != null) {
			String sub_arrayFileContent = arrayFileContent.substring(1, arrayFileContent.length() - 1);
			String rep_sub_arrayFileContent = sub_arrayFileContent.replaceAll("\\[", "");
			rep_sub_arrayFileContent = rep_sub_arrayFileContent.replaceAll("\\]", "");
			String[] arrayFileContentData = rep_sub_arrayFileContent.split(",");
			arrayLength = arrayFileContentData.length;
		}
		return arrayLength;
	}

	/* Checking size attribute of all the locations */

	public void sizeCheck(int loc, int parent, String type) throws IOException, JSONException {
		File location = new File(fusedata_loc + Integer.toString(loc));
		BufferedReader br1 = new BufferedReader(new FileReader(location));
		String dirContent = br1.readLine();
		br1.close();
		JSONObject indirect_json = new JSONObject(dirContent);
		JSONArray array = indirect_json.getJSONArray("filename_to_inode_dict");
		List<Integer> file_loc = new ArrayList<Integer>();
		for (int i = 0; i < array.length(); i++) {						//Looping till all f's and d's
			String inode_type = array.getJSONObject(i).getString("type");
			String inode_name = array.getJSONObject(i).getString("name");
			int inode_location = array.getJSONObject(i).getInt("location");
			if (inode_type.equals("f") && !inode_name.equals(null)) {		//if type=f,then add its location to ArrayList file_loc
				file_loc.add(inode_location);
			}
			if (inode_type.equals("d") && !inode_name.equals(".") && !inode_name.equals("..")) {		//if type=d,call sizeCheck() function
				sizeCheck(inode_location, loc, inode_type);
			}
		}

		for (int j = 0; j < file_loc.size(); j++) {				//looping till total number of type=f files
			File Fpath = new File(fusedata_loc + Integer.toString(file_loc.get(j)));
			BufferedReader br = new BufferedReader(new FileReader(Fpath));
			String fileContent = br.readLine();
			br.close();
			JSONObject file_json = new JSONObject(fileContent);
			int file_size = file_json.getInt("size");
			int file_indirect = file_json.getInt("indirect");
			int file_loc_point = file_json.getInt("location");
			int array_length = getArrayCount(file_loc_point);
			
			/* *	if indirect = 0 & (if file size is negative or if file size > 4096)
			 * 		set file size to a random value between (1 and 4096)
			 * */
			
			if ((file_indirect == 0 && file_size < 0) || (file_indirect == 0 && file_size > BLOCK_SIZE)) {
				int new_file_size = ThreadLocalRandom.current().nextInt(1, BLOCK_SIZE + 1); 	// Generate a random size value
																								// within required range
				fileContent = fileContent.replace("\"size\":" + file_size, "\"size\":" + new_file_size);
				PrintWriter pw1 = new PrintWriter(Fpath, "UTF-8");
				pw1.println(fileContent);
				pw1.close();
				System.out.println("Size --> fusedata." + file_loc.get(j) + " --> updated to " + new_file_size);
			} 
			
			/* *	if indirect = 1 & (if file size is negative or if file size is not according to array length)
			 * 		set file size to a random value between required range
			 * */
			
			else if ((file_indirect == 1 && file_size < 0)
					|| (file_indirect == 1 && file_size <= BLOCK_SIZE * (array_length - 1))
					|| (file_indirect == 1 && file_size > BLOCK_SIZE * array_length)) {
				int new_file_size = ThreadLocalRandom.current().nextInt(BLOCK_SIZE * (array_length - 1), 	// Generate a random size value
						(BLOCK_SIZE * array_length) + 1);													// within required range
				fileContent = fileContent.replace("\"size\":" + file_size, "\"size\":" + new_file_size);
				PrintWriter pw1 = new PrintWriter(Fpath, "UTF-8");
				pw1.println(fileContent);
				pw1.close();
				System.out.println("Size --> fusedata." + file_loc.get(j) + " --> updated to " + new_file_size);
			} else {
				System.out.println("Size --> fusedata." + file_loc.get(j) + " --> correct");
			}
		}
	}
}