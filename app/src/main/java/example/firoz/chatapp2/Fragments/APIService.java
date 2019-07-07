package example.firoz.chatapp2.Fragments;

import example.firoz.chatapp2.Notifications.MyResponse;
import example.firoz.chatapp2.Notifications.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAc-Pf6Go:APA91bGYvvejW_32eMy00zmFTgFf_1_tQnGkOChPw7fidM2TfG-TABzwiYXmutlnM1qpxgtNpNk3nPE7Sl_5GBiOprLQ8BJx2vnj60EX_UvkhYMp7L1GktHK8DBYEbNVt8fCTuvMz8bJ"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
