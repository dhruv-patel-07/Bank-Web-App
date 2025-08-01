package com.bank.web.app.transaction.Service;

import com.bank.web.app.transaction.Repo.AccountRepo;
import com.bank.web.app.transaction.Repo.TransactionRepo;
import com.bank.web.app.transaction.dto.GetTransactionDTO;
import com.bank.web.app.transaction.dto.ResponseDTO;
import com.bank.web.app.transaction.dto.TransactionDataDTO;
import com.bank.web.app.transaction.model.Account;
import com.bank.web.app.transaction.model.Transaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PdfService {

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private FeignService feignService;

    public ByteArrayInputStream SendPdfData(String token, Long accountNum, int month) {

        Account account = accountRepo.findByAccountNum(accountNum);
        if (account == null) {
            return null;
        }

        var userData = feignService.getAccountDetails(token, accountNum);


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
        paragraph4.add("" + account.getBalance());
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
//        LocalDate firstDayOfLastMonth = YearMonth.now().minusMonths(1).atDay(1);
//        LocalDateTime startDateTime = firstDayOfLastMonth.atStartOfDay();

//        LocalDate lastDayOfLastMonth = YearMonth.now().minusMonths(1).atEndOfMonth();
//        LocalDateTime endDateTime = lastDayOfLastMonth.atTime(23, 59, 59);

        LocalDateTime startDateTime = LocalDateTime.now().minusDays(10);
        LocalDateTime endDateTime = LocalDateTime.now();


        List<Transaction> transaction = transactionRepo.findByAccount_AccountNumAndTimeStampBetween(accountNum, startDateTime, endDateTime,Sort.by(Sort.Direction.DESC,"timeStamp"));
        for (Transaction t : transaction) {
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
        return new ByteArrayInputStream(out.toByteArray());
//        System.out.println(transaction);
//        return ResponseEntity.ok().build();
    }

    public List<TransactionDataDTO> SendPdfData1(GetTransactionDTO transactionDTO) throws JsonProcessingException {

        List<Transaction> data =  transactionRepo.findByAccount_AccountNumAndTimeStampBetween(transactionDTO.getAccountNum(), transactionDTO.getStartTime(), transactionDTO.getEndTime(),Sort.by(Sort.Direction.DESC,"timeStamp"));
        List<TransactionDataDTO> response = data.stream().map(m->{
            TransactionDataDTO t = new TransactionDataDTO();
            t.setAmount(m.getAmount());
            t.setRemark(m.getRemark());
            t.setTId(m.getTId());
            t.setTimeStamp(m.getTimeStamp());
            t.setTransactionMethod(m.getTransactionMethod());
            t.setTransactionType(m.getTransactionType());
            t.setAffected_balance(m.getAffected_balance());
            t.setBalance(m.getAccount().getBalance());
            return t;
        }).collect(Collectors.toList());
        return response;
    }
}
