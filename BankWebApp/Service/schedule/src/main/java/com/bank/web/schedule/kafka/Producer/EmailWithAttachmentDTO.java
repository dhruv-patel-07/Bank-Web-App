package com.bank.web.schedule.kafka.Producer;

import lombok.Data;

import java.io.ByteArrayOutputStream;

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
