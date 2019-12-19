package impl;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

import java.util.Objects;

abstract class AbstractDropBoxProvider {

    /*
     * Pristupni KEY kojim se autorizujemo na DropBox nalog.
     * Kreiramo ga preko AppConsole:
     * https://www.dropbox.com/developers/apps
     * Najpre registrujemo aplikaciju, potom generisemo pristupni token.
     */
    //  private static final String ACCESS_TOKEN = "l4BCQC6u5aAAAAAAAAAAE4doXc40NMpBWexvB2gcoys-TGbrDNO0c39eIxdy_g0G";
    private static final String ACCESS_TOKEN = "biVFIY4hjZAAAAAAAAAAEAufdG8Ty3iWNoH2ykQCKFSgUp7zBsKdqJimMzfMYgsS";
    /*
     * Referenca na nalog
     */
    private DbxClientV2 client;

    protected DbxClientV2 getClient() {
        if(Objects.isNull(client)) connect();
        return client;
    }

    private void connect() {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("SoftverskeKomponenteProjekat").build();
        this.client = new DbxClientV2(config, ACCESS_TOKEN);
    }

}
