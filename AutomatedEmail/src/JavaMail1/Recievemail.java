package JavaMail1;


import java.io.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.sql.*;

public class Recievemail {

    public static void AutomatedMail(String username, String password, String ehost, String pdfLocation, String desktopEmail, String accountingEmail) {
        while (true) {
            try {
                Properties properties = new Properties();
                properties.setProperty("mail.store.protocol", "imaps");
                Session emailSession = Session.getDefaultInstance(properties);
                Store emailStore = emailSession.getStore();
                //(host, username, password)
                emailStore.connect("imap."+ehost+".com", username, password); // change the gmail to the correct email.
                // get inbox folder
                Folder emailFolder = emailStore.getFolder("INBOX");
                emailFolder.open(Folder.READ_WRITE);
                Message messages[] = emailFolder.getMessages();
                //connect to database
                try {

                    Class.forName("com.mysql.jdbc.Driver");
                    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/V3?autoReconnect=true&useSSL=false", "root", "1234");
                    Statement stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery("select * from emp");



                    for (int i = messages.length - 1; i >= 0; i--) {
                        Message message = messages[i];


                       boolean seen = false;
                        if(message.isSet(Flags.Flag.SEEN))
                         {
                             System.out.println("Scanning");
                             seen = true;
                         }
                        else
                         {
                        //System.out.println("New Email");
                        Message msg = emailFolder.getMessage(i + 1);
                        Multipart mp = (Multipart) msg.getContent();
                        BodyPart bp = mp.getBodyPart(0);
                        String s = msg.getFrom()[0].toString();
                        String en = (String) bp.getContent();
                        String employeeNumber = en.substring(s.indexOf("EMPLOYEE: ") + 1, en.lastIndexOf("Employee: ") + 16);
                        String newEmpNum = stripNonDigits(employeeNumber);
                        int empNum = Integer.parseInt(newEmpNum);

                        boolean duplicate = false;


                        if (message.getSubject().equals("NEW EMPLOYEE") || message.getSubject().equals("New Employee")) {

                            while (rs.next()) {
                                if (rs.getInt(1) == empNum) {

                                   duplicate = true;
                                }
                                if(duplicate == true){
                                    System.out.println("Already Contains Employee ID");
                                }
                            }

                            if (duplicate == false) {

                                //gets the from email from the <> brackets
                                String emailAddress = s.substring(s.indexOf("<") + 1, s.indexOf(">"));// prints what is in between the two <>
                                System.out.println(emailAddress);

                                //separates the info
                                String string = en;
                                String[] parts = string.split("(?<=\n)");
                                DateFormat dateFormated = new SimpleDateFormat("MM/dd/YYYY");// EE is for the day of the week.EEEE is for full day name
                                Calendar todayD = Calendar.getInstance();
                                todayD.set(Calendar.HOUR_OF_DAY, 0);
                                String theeDate = dateFormated.format(todayD.getTime());

                                //inserts employee number and employee email into database
                                String query = "insert into emp(Employee, EmployeeEmail, EmployeeName, StartDate, Department, JobClassification, Supervisor, Computer, DataBaseEntryDate)" + "values(?,?,?,?,?,?,?,?,?)";
                                PreparedStatement preparedStmt = con.prepareStatement(query);
                                preparedStmt.setInt(1, empNum);
                                preparedStmt.setString(2, emailAddress);
                                preparedStmt.setString(3, parts[1]);
                                preparedStmt.setString(4, parts[2]);
                                preparedStmt.setString(5, parts[3]);
                                preparedStmt.setString(6, parts[4]);
                                preparedStmt.setString(7, parts[5]);
                                preparedStmt.setString(8, parts[6]);


                                preparedStmt.setString(9,theeDate);

                                preparedStmt.execute();
                                System.out.println("Added new Employee");

                                final String newEn = stripNonDigits(employeeNumber);
                                System.out.println("Employee Number: " + newEn);


                                requestPdf("Employee_Request_" + newEn, (String) bp.getContent(), emailAddress);
                                // System.out.println(employeeNumber);

                                //System.out.print(result.toString());			// gets message




                                // SMTP info
                                String host = "smtp."+ehost+".com";
                                String port = "465";
                                String mailFrom = username;                //username
                                String passwords = password;                        //password

                                DateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy");// EE is for the day of the week.EEEE is for full day name
                                Calendar today = Calendar.getInstance();
                                today.set(Calendar.HOUR_OF_DAY, 0);
                                String theDate = dateFormat.format(today.getTime());
                                //System.out.println(theDate);	//used for testing output
                                String newDate = null;
                                if (theDate.contains("Thursday") || theDate.contains("Wednesday") || theDate.contains("Friday")) {

                                    today.add(Calendar.DAY_OF_YEAR, 5);
                                    newDate = dateFormat.format(today.getTime());
                                    // System.out.println(newDate); //used for testing output

                                } else if (theDate.contains("Saturday")) {
                                    today.add(Calendar.DAY_OF_YEAR, 4);
                                    newDate = dateFormat.format(today.getTime());
                                    //System.out.println(newDate);	// used for testing output
                                } else {
                                    today.add(Calendar.DAY_OF_YEAR, 3);
                                    newDate = dateFormat.format(today.getTime());
                                    //System.out.println(newDate);	// used for testing output
                                }


                                // message info
                                String mailTo = emailAddress;
                                String Desktop = desktopEmail; // Desktop "Change the email in this line to the email of the desktop"
                                String subject = "New Employee Laptop Request Confirmation Email DO_NOT_REPLY";
                                String Subject2 = "New Employee Laptop Request " + newEn;
                                String Estimated = ("Your request has been sent to " + Desktop + ". Estimated Delivery Date is: " + newDate + ".");
                                String messagess = Estimated;
                                String desktop = desktopEmail;
                                String messagesss = "";
                                String Messages1 = "Employee notified that request has been sent on: " + theDate + ". The Employee is also notified that the estimated Delivery date is: " + newDate;
                                String Messages2 = parts[1];
                                String Messages3 = parts[2];
                                String Messages4 = parts[3];
                                String Messages5 = parts[4];
                                String Messages6 = parts[5];
                                String Messages7 = parts[6];
                                // attachments
                                String[] attachFiles = new String[1];
                                attachFiles[0] = pdfLocation+ "Employee_Request_" + newEn + ".pdf"; //location of pdf
                                try {
                                    sendEmailWithAttachments(host, port, mailFrom, passwords, mailTo,
                                            subject, messagess, messagesss, attachFiles);
                                    sendEmailWithAttachments2(host, port, mailFrom, passwords, desktop,
                                            Subject2, Messages1, Messages2, Messages3, Messages4, Messages5, Messages6, Messages7, attachFiles, empNum);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                                // message.setFlag(Flags.Flag.DELETED, true); this is used for deleting mail
                            }
                        } // end of "New Employee" if statement

                        if (message.getSubject().contains("Re: NEW EMPLOYEE LAPTOP REQUEST") || message.getSubject().contains("Re: New Employee Laptop Request")) {

                            String dd = (String) bp.getContent();
                            String[] partz = dd.split("(?<=\n)");
                            String deliveryDate = partz[7];


                            //String deliveryDate = dd.substring(s.indexOf("DELIVERY DATE: ") , dd.indexOf("DELIVERY DATE: "+11));
                            final String newDelivery = addPeriods(stripNonDigits(deliveryDate));

                            //System.out.println(newDelivery);
                            final String newEn = stripNonDigits(employeeNumber);

                            System.out.println("Asset PDF Name: " + newEn + "-" + newDelivery);
                            String assetPDFname = newEn + "-" + newDelivery;
                            AssetPdf(assetPDFname, (String) bp.getContent(), assetPDFname);
                            // make asset pdf for accounting and send to accounting
                            // asset pdf must be named "employee number + delivery date" i.e. 95131-20170515
                            String host = "smtp."+ehost+".com";
                            String port = "465";
                            String mailFrom = username;                //username
                            String passwords = password;
                            String mailTo = accountingEmail; // Accounting
                            String subject = "Employee laptop request asset pdf";
                            String messagess = "Asset PDF for accounting";
                            String[] attachFiles = new String[1];
                            attachFiles[0] = pdfLocation + assetPDFname + ".pdf ";
                            sendEmailWithAttachments(host, port, mailFrom, passwords, mailTo, subject, messagess, "", attachFiles);
                            //  message.setFlag(Flags.Flag.DELETED, true); this was used for deleting mail
                        }

                    }   }
                    con.close();
                } catch (Exception e) {
                    System.out.println(e);
                }
                emailFolder.close(true);
                emailStore.close();

            } catch (Exception mex) {
                mex.printStackTrace();

            }

        }
    }

    public static String addPeriods(String addPeriods) {
        final StringBuilder sb = new StringBuilder(addPeriods.length());
        for (int i = 0; i < addPeriods.length(); i++) {
            final char c = addPeriods.charAt(i);
            if (i != 2 && i != 5) {
                sb.append(c);
            } else {
                sb.append(c);
                sb.insert(i, ".");
            }
        }
        return sb.toString();
    }

    public static String stripNonDigits(String getDigits) {
        final StringBuilder sb = new StringBuilder(getDigits.length());
        for (int i = 0; i < getDigits.length(); i++) {
            final char c = getDigits.charAt(i);
            if (c > 47 && c < 58) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static void requestPdf(String filename, String body, String email) throws DocumentException, IOException {

        Document request = new Document();
        PdfWriter.getInstance(request, new FileOutputStream(filename + ".pdf"));
        request.open();
        request.add(new Paragraph("NEW EMPLOYEE COMPUTER REQUEST"));
        request.add(new Paragraph("Employee Email: <" + email + ">"));
        request.add(new Paragraph(" "));
        request.add(new Paragraph(body));
        request.add(new Paragraph("Status:"));
        request.add(new Paragraph("Delivery Date:"));
        request.close();
    }

    public static void AssetPdf(String filename, String body, String assetPDF) throws DocumentException, IOException {

        Document request = new Document();
        PdfWriter.getInstance(request, new FileOutputStream(filename + ".pdf"));
        request.open();
        request.add(new Paragraph("NEW EMPLOYEE COMPUTER REQUEST DELIVERED"));

        request.add(new Paragraph(" "));
        request.add(new Paragraph(body));
        request.add(new Paragraph("Asset Tag: " + assetPDF));

        request.close();
    }

    public static void sendEmailWithAttachments(String host, String port, final String userName, final String password,
                                                String toAddress, String subject, String message, String message2, String[] attachFiles)
            throws AddressException, MessagingException {
        // sets SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.user", userName);
        properties.put("mail.password", password);
        properties.put("mail.smtp.socketFactory.fallback", "false");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        // creates a new session with an authenticator
        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        };
        Session session = Session.getInstance(properties, auth);

        // creates a new e-mail message
        Message msg = new MimeMessage(session);

        msg.setFrom(new InternetAddress(userName));
        InternetAddress[] toAddresses = {new InternetAddress(toAddress)};
        msg.setRecipients(Message.RecipientType.TO, toAddresses);
        msg.setSubject(subject);
        msg.setSentDate(new Date());

        // creates message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(message, "text/html");
        // for gap (styling)
        MimeBodyPart gap = new MimeBodyPart();
        gap.setContent("", "text/html");
        // for gap (styling)
        MimeBodyPart messageBodyPart2 = new MimeBodyPart();
        messageBodyPart2.setContent(message2, "text/html");

        // creates multi-part
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        multipart.addBodyPart(gap);
        multipart.addBodyPart(messageBodyPart2);

        // adds attachments
        if (attachFiles != null && attachFiles.length > 0) {
            for (String filePath : attachFiles) {
                MimeBodyPart attachPart = new MimeBodyPart();

                try {
                    attachPart.attachFile(filePath);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                multipart.addBodyPart(attachPart);
            }
        }

        // sets the multi-part as e-mail's content
        msg.setContent(multipart);

        // sends the e-mail
        Transport.send(msg);

    }

    public static void sendEmailWithAttachments2(String host, String port, final String userName, final String password,
                                                 String toAddress, String subject, String message, String message2, String message3, String message4, String message5, String message6, String message7, String[] attachFiles,int EMP)
            throws AddressException, MessagingException {
        // sets SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.user", userName);
        properties.put("mail.password", password);
        properties.put("mail.smtp.socketFactory.fallback", "false");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        // creates a new session with an authenticator
        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        };
        Session session = Session.getInstance(properties, auth);

        // creates a new e-mail message
        Message msg = new MimeMessage(session);

        msg.setFrom(new InternetAddress(userName));
        InternetAddress[] toAddresses = {new InternetAddress(toAddress)};
        msg.setRecipients(Message.RecipientType.TO, toAddresses);
        msg.setSubject(subject);
        msg.setSentDate(new Date());

        // creates message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(message, "text/html");
        // for gap (styling)
        MimeBodyPart gap = new MimeBodyPart();
        gap.setContent("", "text/html");
        // for gap (styling)
        MimeBodyPart messageBodyPart2 = new MimeBodyPart();
        MimeBodyPart messageBodyPart3 = new MimeBodyPart();
        messageBodyPart3.setContent("EMPLOYEE: "+EMP, "text/html");
        messageBodyPart2.setContent(message2, "text/html");
        MimeBodyPart messageBodyPart4 = new MimeBodyPart();
        messageBodyPart4.setContent("DELIVERY DATE: ", "text/html");
        MimeBodyPart messageBodyPart5 = new MimeBodyPart();
        messageBodyPart5.setContent(message3, "text/html");
        MimeBodyPart messageBodyPart6 = new MimeBodyPart();
        messageBodyPart6.setContent(message4, "text/html");
        MimeBodyPart messageBodyPart7 = new MimeBodyPart();
        messageBodyPart7.setContent(message5, "text/html");
        MimeBodyPart messageBodyPart8 = new MimeBodyPart();
        messageBodyPart8.setContent(message6, "text/html");
        MimeBodyPart messageBodyPart9 = new MimeBodyPart();
        messageBodyPart9.setContent(message7,"text/html");

        // creates multi-part
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart3);
        multipart.addBodyPart(messageBodyPart2);

        multipart.addBodyPart(messageBodyPart5);
        multipart.addBodyPart(messageBodyPart6);
        multipart.addBodyPart(messageBodyPart7);
        multipart.addBodyPart(messageBodyPart8);
        multipart.addBodyPart(messageBodyPart9);
        multipart.addBodyPart(messageBodyPart4);
        multipart.addBodyPart(gap);
        multipart.addBodyPart(messageBodyPart);


        // adds attachments
        if (attachFiles != null && attachFiles.length > 0) {
            for (String filePath : attachFiles) {
                MimeBodyPart attachPart = new MimeBodyPart();

                try {
                    attachPart.attachFile(filePath);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                multipart.addBodyPart(attachPart);
            }
        }

        // sets the multi-part as e-mail's content
        msg.setContent(multipart);

        // sends the e-mail
        Transport.send(msg);

    }

    public static void main(String args[]) {
        AutomatedMail("~Main Email to automate~", "The password to main email","gmail","C:/Users/Ryan Hong/AutomatedEmail/", "~insert desktop email~","~Insert Accounting Email~"); // when putting in the pdfLocation make sure you end with a slash.

    }
}
