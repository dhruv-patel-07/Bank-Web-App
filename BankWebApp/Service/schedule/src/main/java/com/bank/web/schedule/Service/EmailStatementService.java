package com.bank.web.schedule.Service;

import com.bank.web.schedule.dto.TransactionDataDTO;
import com.bank.web.schedule.kafka.Producer.EmailWithAttachmentDTO;
import com.bank.web.schedule.kafka.Producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailStatementService {
    @Autowired
    private FeignService feignService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KafkaProducer producer;

    @Async
    public void EmailStatement(String token, Long accountNum, List<TransactionDataDTO> transaction, String emailTitle,String startTime,String endTime){
        var userData = feignService.getAccountDetails(token, accountNum);
//
        String url = "http://TRANSACTION-SERVICE/api/v1/transaction/check-balance";
        HttpHeaders headers =  new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        Map<String, Object> requestBody = Map.of(
                "accountNum", accountNum,
                "uid", userData.getUid()
        );
        HttpEntity<Map<String,Object>> reHttpEntity = new HttpEntity<>(requestBody,headers);
        ResponseEntity<Double> response = restTemplate.exchange(url, HttpMethod.POST,reHttpEntity, Double.class);
        String accountBalance = String.valueOf(response.getBody());

        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", userData.getName());
        userMap.put("account", userData.getAccountNum());
        userMap.put("branch", userData.getBranch());
        userMap.put("type", userData.getType());


        String name = "Account holder name : ";
        String title = "Bank Statement";
        String AccountNumber = "Account number  : ";
        String IFSC = "Branch  : ";
        String Type = "Account type : ";
        String balance = "Balance : ";
        String From = "From : ";
        String To = "To : ";

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();

        Paragraph spacer = new Paragraph();

        PdfWriter.getInstance(document, out);
        document.open();

        Font RedFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.RED);
        Font GreenFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.GREEN);
        Font defaultFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
        Font header = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);


        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
        Paragraph titlePara = new Paragraph(title, titleFont);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        document.add(titlePara);

        spacer.setSpacingBefore(10f); // 100pt space before next element
        document.add(spacer);

        Font paraFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Paragraph paragraph = new Paragraph(name, paraFont);
        paragraph.add(new Chunk(userMap.get("name")));
        document.add(paragraph);

        Paragraph paragraph1 = new Paragraph(AccountNumber, paraFont);
        paragraph1.add(new Chunk(userMap.get("account")));
        document.add(paragraph1);

        Paragraph paragraph2 = new Paragraph(IFSC, paraFont);
        paragraph2.add(new Chunk(userMap.get("branch")));
        document.add(paragraph2);

        Paragraph paragraph3 = new Paragraph(Type, paraFont);
        paragraph3.add(new Chunk(userMap.get("type")));
        document.add(paragraph3);

        Paragraph paragraph5 = new Paragraph(From, paraFont);
        Paragraph paragraph6 = new Paragraph(To, paraFont);

        Font paraFont1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
        Paragraph paragraph4 = new Paragraph(balance, paraFont1);
        paragraph4.add(accountBalance);
        document.add(paragraph4);

        spacer.setSpacingBefore(20f);
        document.add(spacer);


        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(95);


        float[] columnWidths = {1f, 2f, 3f, 1f, 2f, 2f, 2f};
        table.setWidths(columnWidths);

        table.addCell(new PdfPCell(new Phrase("NO", header))).setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(new PdfPCell(new Phrase("Transaction id", header))).setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(new PdfPCell(new Phrase("TimeStamp", header))).setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(new PdfPCell(new Phrase("Type", header))).setHorizontalAlignment(Element.ALIGN_CENTER);
//        table.addCell(new PdfPCell(new Phrase("Amount", header)));
        table.addCell(new PdfPCell(new Phrase("withdrawal", header))).setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(new PdfPCell(new Phrase("Deposit", header))).setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(new PdfPCell(new Phrase("Closing Balance.", header))).setHorizontalAlignment(Element.ALIGN_CENTER);

        int counter = 1;

        for (TransactionDataDTO t : transaction) {
            table.addCell(new PdfPCell(new Phrase("" + counter, defaultFont)));
            table.addCell(new PdfPCell(new Phrase("" + t.getTId(), defaultFont)));
            table.addCell(new PdfPCell(new Phrase("" + t.getTimeStamp(), defaultFont)));
            table.addCell(new PdfPCell(new Phrase("" + t.getTransactionType().toLowerCase(), defaultFont)));
            if (t.getTransactionType().equalsIgnoreCase("debit")) {
                table.addCell(new PdfPCell(new Phrase("" + t.getAmount(), RedFont))).setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(new PdfPCell(new Phrase("", defaultFont)));

            } else {
                table.addCell(new PdfPCell(new Phrase("", defaultFont)));
                table.addCell(new PdfPCell(new Phrase("" + t.getAmount(), GreenFont))).setHorizontalAlignment(Element.ALIGN_CENTER);

            }
            table.addCell(new PdfPCell(new Phrase("" + t.getAffected_balance(), defaultFont))).setHorizontalAlignment(Element.ALIGN_CENTER);
            counter++;
        }
        document.add(table);
        document.close();

        EmailWithAttachmentDTO email = new EmailWithAttachmentDTO();
        email.setEmail(userData.getEmail());
//        email.setEmail("dap21042003@gmail.com");
        email.setTitle(emailTitle);
        email.setFileData(out.toByteArray());
        email.setFileName(accountNum+".pdf");
        email.setAccountNum(accountNum.toString());
        email.setEndTime(endTime);
        email.setStartTime(startTime);
        producer.StatementEmail(email);
//        return out;
    }

    }


