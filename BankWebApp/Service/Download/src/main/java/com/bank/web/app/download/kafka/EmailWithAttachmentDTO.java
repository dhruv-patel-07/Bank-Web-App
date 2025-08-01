package com.bank.web.app.download.kafka;

import lombok.Data;

@Data
public class EmailWithAttachmentDTO {
        private String email;
        private String fileName;
        private byte[] fileData;
        private String title;
        private String startTime;
        private String endTime;
        private String accountNum;

}
