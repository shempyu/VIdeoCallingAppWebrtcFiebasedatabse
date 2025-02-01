package com.example.webrtccalling

import com.google.auth.oauth2.GoogleCredentials
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets

object AccessToken {
    private val firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging"
    fun getAccessToken(): String?{
        try{
            val jsonString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"instagram-practice-833e0\",\n" +
                    "  \"private_key_id\": \"4864fc9c9f9a550ffeb069cf093b9266124b9802\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQChLxTOJRmk8Nhx\\nxI8lICRDRUQKkhKtyeSxVswEGjUIWxdaq2cgriKmMr0lOlA8dnA+ywmHRtVt1qx0\\n8g3OCrC2t6Fdxc811YZGO6px3R4UKc6fMDNpaj+cUb8naeMrSizh9xC5JjnMQPrq\\nSoZlher7p23TiA3uo2L01P+gWNhgg63392lI3tcBGOaYuVsemIJL87BQgNb101qT\\n0mGeH83CwrcJKb/JeQWeNQN6uXxjvJUwltRzA24Wj7RezKfDZSYO503JWxcSL2ri\\nS9KEHk1pRsC1IBf+xhc9kA5MzmShk+8fTxIhVBzBoTr/rLazKYs42nvstTV1gW0B\\nlX6zBucVAgMBAAECggEADYNrfJ76p+vzUZJ3p9aqg6NOgY5el4hqLn4G678gvBsa\\n+aSOKFzBFesCFZlueas6XOFb4qYLJcLOQZ27j48w4pjBpCF97lcBA2lkMFin+WvJ\\n4WnCna9lNt9Zpd3ISGxXF/jFQ6iTRu8ZukPWQbO5EDEo0YfCF0RGex1ScyiPt4Og\\njce4NImEOLniISypbEBGfQewCrRH/+RBw014tJ0HF5zMF951xNExY68jr7fP6P4f\\nEMyYhtbeBDhbeokrAP+q3jRCt3dDhr2JLWpc4SXohl7C9qUhe7S074QkIZvGRNox\\nNUgl8INGyCgWXS9mdToGZEOt9QODToUGVdBB1+ya4QKBgQC8hRQoFONZaeCgheje\\n+p0TDpBvwJlLrV/CFAuXhWBF2ntfXmjt2f1RyG+7jQLgz5D/z8K3My6ituMUV824\\nXv6LERtHE9JC+5cAe58VmhiQMoIJeWUt69DVKJYM+qJGlc7/RNdzTuaFsSmCTfnc\\n4yY8sFojAzvY1ithS0PEIV0wpQKBgQDa4RalewBq03tTjyetamuMEOhK3S+E8D2o\\ngC9Xc/brVAcm/m4W5u9zZ2qAnUumU81HSy3V2Zz73znWGjVOYrcrVuvsEjrzQhYO\\nDtLWJKscCgDfs+7ntJmsxHBLpZ+CHqoG9tk4ba+e3yH9uxkRLmjBZHWHE2KQOLmY\\nrxfaqZchsQKBgDvGRnnfBadYNu0vCIOLGzrrp0iH3RNtyasUCAjnNXtwpyW6HbMt\\nJ3FDCIqDT6JQrj0udyL3i5X0P1Q38va+yB74MZn74vNyKpLwn6SN07QFD85JO5ev\\n6etsjwdWgRW4coXmIhphrh496ldyugMb0wfzMuFfKR676c1TvAz4N0AxAoGBAM0r\\nYQcNHX8Fbf0Nm6u6rcVFs/k5DGoYqDNitP3M/g/pXCr5JEjjgGE092hICbmHpEzH\\n89cZfUW8IYLIXl9zuS8u80b+b7PGptXWn1uMU7icx+TpzW/0oniISpseTT6zb73H\\ndiam6pCJqFsnHoA0eI2PlXtgeGuoQJEWz81wWvKBAoGAHmfHxYOfYqcy6rW+svts\\n3ndIjnXP2+Z5Tl8lPqR4FQzm8NSI6QWnGG9iYGIoJ6MpFHlipx1EOhzoaeRu2Hvy\\nlRypPeiqSODzmG44NCt8TTbmuIU4apOAWUfueX6c+xQOACBEYxWUDODdYjWOw2el\\nyxusZYIah4mLQM3MXiQsZ+U=\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-jy5p4@instagram-practice-833e0.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"116871797981537989254\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-jy5p4%40instagram-practice-833e0.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}\n"
            val stream = ByteArrayInputStream(jsonString.toByteArray(StandardCharsets.UTF_8))
            val googleCredential = GoogleCredentials.fromStream(stream)
                .createScoped(arrayListOf(firebaseMessagingScope))
            googleCredential.refresh()

            return googleCredential.accessToken.tokenValue
        }catch (e: IOException){
            return null
        }
    }
}