package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		//listFilesを使用してfilesという配列に、
		//指定してパスに存在するすべてのファイル(または、ディレクトリ)の情報を格納します。
		File[] files = new File("C:\\Users\\trainee1476\\Desktop\\売り上げ集計課題").listFiles();

		//先にファイルの情報を格納する List(ArrayList) を宣言します。
		List<File> rcdFiles = new ArrayList<>();
		List<String> Data = new ArrayList<>();
		List<String>  Branchcode= new ArrayList<>();


		//filesの数だけ繰り返すことで、
		//指定したパスに存在するすべてのファイル(または、ディレクトリ)の数だけ繰り返されます。
		for(int i = 0; i < files.length ; i++) {
			//files[i].getName()でファイル名が取得できます。
			files[i].getName();
			if(files[i].getName().matches("[0-9]{8}.+rcd$")) {
				//売上ファイルの条件に当てはまったものだけ、List(ArrayList)に追加します。
					rcdFiles.add(files[i]);
			}
		}

		//rcdFilesに複数の売上ファイルの情報を格納しているので、その数だけ繰り返します。
		for(int i = 0; i < rcdFiles.size(); i++) {

			//支店定義ファイル読み込み(readFileメソッド)を参考に売上ファイルの中身を読み込みます。
			//売上ファイルの1行目には支店コード、2行目には売上金額が入っています。
			 try {
		            // ファイルのパスを指定する
		            File file = new File("C:\\Users\\trainee1476\\Desktop\\売り上げ集計課題"
		            		,rcdFiles.get(i).getName());

		            // ファイルが存在しない場合に例外が発生するので確認する
		            if (!file.exists()) {
		                System.out.print("ファイルが存在しません");
		                return;
		            }

		            // BufferedReaderクラスのreadLineメソッドを使って1行ずつ読み込み表示する
		            FileReader fileReader = new FileReader(file);
		            BufferedReader bufferedReader = new BufferedReader(fileReader);
		            String data;
		            while ((data = bufferedReader.readLine()) != null) {
		            	if(data.matches("^([1-9][0-9]*)$")){
		            		System.out.println(data);
		            		Data.add(data);
		            	}else if(data.matches("^[0-9]*$")){
		            		Branchcode.add(data);
		            	}
		            }
		          //売上ファイルから読み込んだ売上金額をMapに加算していくために、型の変換を行います。
	    			//※詳細は後述で説明
	    			long fileSale = Long.parseLong(Data.get(i));

		          //読み込んだ売上金額を加算します。
	    			//※詳細は後述で説明
	    			Long saleAmount = branchSales.get(Branchcode.get(i)) + fileSale;

			        //加算した売上金額をMapに追加します。
		            branchSales.put(Branchcode.get(i),saleAmount);

		            // 最後にファイルを閉じてリソースを開放する
		            bufferedReader.close();

		        } catch (IOException e) {
		            e.printStackTrace();
		        }

		}

		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

	}


	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String[] items = line.split(",");

				//Mapに追加する2つの情報を putの引数として指定します。
				branchNames.put(items[0], items[1]);
				branchSales.put(items[0],(long) 0);
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)


		try {
			File file = new File("C:\\Users\\trainee1476\\Desktop\\売り上げ集計課題","branch.out");
			if (file.createNewFile()) {
	            System.out.println("File created: " + file.getName());
			}
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			for (String key : branchNames.keySet()) {
				//keyという変数には、Mapから取得したキーが代入されています。
				//拡張for⽂で繰り返されているので、1つ⽬のキーが取得できたら、
				//2つ⽬の取得...といったように、次々とkeyという変数に上書きされていきます。
				bw.write(key + "、" + branchNames.get(key) + "、" + branchSales.get(key));
				bw.newLine();
			}
			bw.close();
		}catch(IOException e){
				e.printStackTrace();
		}finally {

		}


		return true;
	}


}
