package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
	private static final String FILE_NOT_NUMBER = "売上ファイル名が連番になっていません";
	private static final String AMOUNT_EXCEED_DIGIT = "合計金額が10桁を超えました";
	private static final String FILE_INVALID_CODE = "支店定義ファイルの支店コードが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			//コマンドライン引数が1つ設定されていなかった場合は、
			//エラーメッセージをコンソールに表⽰します。
			System.out.println(UNKNOWN_ERROR);
			return;
		}

		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if (!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		//listFilesを使用してfilesという配列に、
		//指定してパスに存在するすべてのファイル(または、ディレクトリ)の情報を格納します。
		File[] files = new File(args[0]).listFiles();

		//先にファイルの情報を格納する List(ArrayList) を宣言します。
		List<File> rcdFiles = new ArrayList<>();

		//filesの数だけ繰り返すことで、
		//指定したパスに存在するすべてのファイル(または、ディレクトリ)の数だけ繰り返されます。
		for (int i = 0; i < files.length; i++) {
			//files[i].getName()でファイル名が取得できます。

			if (files[i].isFile() && files[i].getName().matches("^[0-9]{8}[.]rcd$")) {
				//売上ファイルの条件に当てはまったものだけ、List(ArrayList)に追加します。
				rcdFiles.add(files[i]);
			}
		}
		for (int i = 1; i < rcdFiles.size(); i++) {
			//売上ファイルを保持しているListをソート
			Collections.sort(rcdFiles);

			int former = Integer.parseInt(rcdFiles.get(i - 1).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));

			//比較する2つのファイル名の先頭から数字の8文字を切り出し、int型に変換します。
			if ((latter - former) != 1) {
				//2つのファイル名の数字を比較して、差が1ではなかったら、
				//エラーメッセージをコンソールに表示します。
				System.out.println(FILE_NOT_NUMBER);
				return;
			}

		}

		//rcdFilesに複数の売上ファイルの情報を格納しているので、その数だけ繰り返します。
		for (int i = 0; i < rcdFiles.size(); i++) {
			List<String> fileContents = new ArrayList<>();
			BufferedReader bufferedReader = null;
			//支店定義ファイル読み込み(readFileメソッド)を参考に売上ファイルの中身を読み込みます。
			//売上ファイルの1行目には支店コード、2行目には売上金額が入っています。
			try {
				// ファイルのパスを指定する
				File file = new File(args[0], rcdFiles.get(i).getName());

				// ファイルが存在しない場合に例外が発生するので確認する
				if (!file.exists()) {
					System.out.print(FILE_NOT_EXIST);
					return;
				}

				// BufferedReaderクラスのreadLineメソッドを使って1行ずつ読み込み表示する
				FileReader fileReader = new FileReader(file);
				bufferedReader = new BufferedReader(fileReader);
				String data;
				while ((data = bufferedReader.readLine()) != null) {
					fileContents.add(data);
				}
				if (fileContents.size() != 2) {
					//売上ファイルの行数が2行ではなかった場合は、
					//エラーメッセージをコンソールに表示します。
					System.out.println(FILE_INVALID_FORMAT);
					return;
				}
				if (!branchSales.containsKey(fileContents.get(0))) {
					//支店情報を保持しているMapに売上ファイルの支店コードが存在しなかった場合は、
					//エラーメッセージをコンソールに表⽰します。
					System.out.println(FILE_INVALID_CODE);
					return;
				}
				//売上ファイルから読み込んだ売上金額をMapに加算していくために、型の変換を行います。
				//※詳細は後述で説明
				long fileSale = Long.parseLong(fileContents.get(1));
				if (!fileContents.get(1).matches("^[0-9]*$")) {
					//売上⾦額が数字ではなかった場合は、
					//エラーメッセージをコンソールに表⽰します。
					System.out.println(UNKNOWN_ERROR);
					return;
				}
				//読み込んだ売上金額を加算します。
				//※詳細は後述で説明
				Long saleAmount = branchSales.get(fileContents.get(0)) + fileSale;
				if (saleAmount >= 10000000000L) {
					System.out.println(AMOUNT_EXCEED_DIGIT);
					return;
				}

				//加算した売上金額をMapに追加します。
				branchSales.put(fileContents.get(0), saleAmount);

			} catch (IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return;
				}
			}

		}

		// 支店別集計ファイル書き込み処理
		if (!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
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
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {
			File file = new File(path, fileName);
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while ((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String[] items = line.split(",");

				if ((items.length != 2) || (items[0].matches("^[0-9]{3}!$"))) {
					//⽀店定義ファイルの仕様が満たされていない場合、
					//エラーメッセージをコンソールに表⽰します。
					System.out.println(FILE_INVALID_FORMAT);
					return false;
				}
				//Mapに追加する2つの情報を putの引数として指定します。
				branchNames.put(items[0], items[1]);
				branchSales.put(items[0], 0L);
			}

		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch (IOException e) {
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
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)

		BufferedWriter bw = null;
		try {
			File file = new File(path, fileName);
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			for (String key : branchNames.keySet()) {
				//keyという変数には、Mapから取得したキーが代入されています。
				//拡張for⽂で繰り返されているので、1つ⽬のキーが取得できたら、
				//2つ⽬の取得...といったように、次々とkeyという変数に上書きされていきます。
				bw.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));
				bw.newLine();
			}
		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			try {
				bw.close();
			} catch (IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return false;
			}
		}

		return true;
	}

}
