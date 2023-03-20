package Api;

import Mode.NotificacaoDados;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificacaoService {
    @Headers({
            "Authorization:key=AAAAjDkwNo4:APA91bFTw_khjWv6D_jR03qgl2Pgn_sfOq3CsHCJ45payhUABUMmksr7Gag11MOII8064-Y1Zd9qq_Xni4IgF8qd-xALSVXOMYUA4VK7BAffcV8rEHwJfYzp1ckBv6ME75-4Wo4wF0hI",
            "Content-Type:application/json"
    })


    @POST("send")
    Call<NotificacaoDados> salvarNotificacao(@Body NotificacaoDados notificacaoDados);
}

