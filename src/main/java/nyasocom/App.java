package nyasocom;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class MyHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(final Thread t, final Throwable ex) {
        System.out.println("例外発生 : " + t.getId());
        ex.printStackTrace();
    }
}

public class App extends Thread {

    @SuppressWarnings({ "varargs" })

    public static void main(final String[] args) throws IOException {

        ReentrantReadWriteLock locks;
        locks = new ReentrantReadWriteLock();

        final ReentrantLock lock;
        lock = new ReentrantLock();
        Lock locker;
        locker = locks.writeLock();

        // 非同期処理を使う
        final App thread = new App();
        thread.setUncaughtExceptionHandler(new MyHandler());
        thread.setDaemon(true);
        thread.start();

        // ファイルが読み込み可能か調べる
        System.out.println("---------------------------------------------------------------");
        System.out.println();
        System.out.println("調べたいディレクトリのPATHとファイル名を指定してください...");
        System.out.println();
        System.out.println("---------------------------------------------------------------");
        System.out.println();
        System.out.print("> ");
        final BufferedReader infile = new BufferedReader(new InputStreamReader(System.in));
        final File file = new File(infile.readLine());
        System.out.println();
        final boolean canRead = file.canRead();
        System.out.println("---------------------------------------------------");
        System.out.println();
        System.out.println("読み込み可・不可を判定 : " + canRead);
        System.out.println();
        System.out.println("---------------------------------------------------");
        System.out.println();
        // 最初の行を空白に
        System.out.println();
        System.out.println("---------------------------------------------------");
        System.out.println();
        System.out.println("検索する文字列を入力できます...");
        System.out.println();
        System.out.println("---------------------------------------------------");
        System.out.println();
        System.out.print("> ");

        try {
            // 読み込みがfalseのとき、例外発生。
            if (!canRead) {
                throw new Exception();
            }

            // ロック処理、更新処理
            locker.lock();
            lock.lock();

            String env;

            // readerにutf-8をセット
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader((System.in), StandardCharsets.UTF_8));

            env = reader.readLine();

            // ファイルを読み込む
            final FileReader filer = new FileReader(file);
            final BufferedReader buffer = new BufferedReader(filer);

            String str;
            int num = 0;

            while ((str = buffer.readLine()) != null) {

                // 行番号加算
                num++;

                // 検索する文字列 or 正規表現パターン
                final Pattern pattern = Pattern.compile(env);
                final Matcher matcher = pattern.matcher(str);

                while (matcher.find()) {
                    System.out.println(num + " " + str);
                }
            }

            buffer.close();
            filer.close();
            thread.join();

        } catch (final Exception e) {
            //e.printStackTrace();
            System.out.println("ファイルを正常に読み込めませんでした。");
            thread.interrupt();

        } finally {

            /* ロック開放 */
            locker.unlock();
            System.out.println();
            System.out.println("---------------------------------------------------");
        }
    }
}
