package sample.client;

/**
 * 単純なHTTP経由の実行検証。
 * <p>SpringがサポートするWebTestSupportでの検証で良いのですが、コンテナ立ち上げた後に叩く単純確認用に作りました。
 * <p>「extention.security.auth.enabled: true」の時は実際にログインして処理を行います。
 * falseの時はDummyLoginInterceptorによる擬似ログインが行われます。
 */
public class SampleClient {
    //private static final String ROOT_PATH = "http://localhost:8080/api";

}
