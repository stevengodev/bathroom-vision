package com.foliaco.vision_bathroom.firebase;

import com.foliaco.vision_bathroom.dto.PushNotificationRequest;

public interface NotificationService {

    void notifyBathroomStatusChanged(PushNotificationRequest request);

}
