import bloodbank.service.*;
import bloodbank.model.*;
import bloodbank.utility.*;
import bloodbank.ui.Menu;
import java.time.*;
import java.nio.file.*;
import java.io.*;
import java.util.*;

public class PatientWorkflowTest {
    static int checks;
    static void check(boolean value, String label) { checks++; if (!value) throw new AssertionError(label); }
    static void denied(Runnable action) { boolean failed=false; try { action.run(); } catch(IllegalArgumentException | IllegalStateException e) { failed=true; } check(failed,"Expected rejection"); }
    public static void main(String[] args) throws Exception {
        Path path=Files.createTempDirectory("patient-workflow-");
        Clock clock=Clock.systemDefaultZone();
        FileManager files=new FileManager(path);
        BloodBankService s=new BloodBankService(DemoData.create(),files,clock);
        s.selectRole("HOSPITAL");
        Patient p=s.createPatient("New patient",30,"Other","1234567890","Address","B+","Treatment","Dr Test",3);
        check(p.getPersonId().equals("PAT002"),"Unique patient ID");
        BloodRequest r=s.requestBlood(p.getPersonId(),"HIGH");
        check(r.getUnitsRequested()==3 && r.getBloodGroup().equals("B+"),"Quantity and group from patient");
        BloodRequest duplicate = s.requestBlood(p.getPersonId(),"LOW");
        s.issueBlood(r.getTransactionId());
        check(s.requests().get(0).getStatus().equals("COMPLETED"),"Completed");
        check(s.ownInventory().getStockForGroup("B+",s.today())==2,"Deduct exactly three");
        denied(()->s.issueBlood(r.getTransactionId()));
        check(s.ownInventory().getStockForGroup("B+",s.today())==2,"No double issue");
        check(s.patients().stream().filter(q->q.getPersonId().equals(p.getPersonId())).findFirst().orElseThrow().getDetails().contains("Status: COMPLETED"),"Patient display completed");
        check(s.patientsAwaitingBlood().stream().noneMatch(q->q.getPersonId().equals(p.getPersonId())),"Completed patient excluded from request choices");
        denied(()->s.requestBlood(p.getPersonId(),"LOW"));
        denied(()->s.issueBlood(duplicate.getTransactionId()));
        Patient waiting = s.createPatient("Waiting patient",30,"Other","1234567890","Address","B+","Treatment","Dr Test",3);
        BloodRequest shortage=s.requestBlood(waiting.getPersonId(),"LOW");
        denied(()->s.issueBlood(shortage.getTransactionId()));
        check(s.requests().stream().filter(q->q.getTransactionId().equals(shortage.getTransactionId())).findFirst().orElseThrow().getStatus().equals("PENDING"),"Shortage remains pending");
        check(s.ownInventory().getStockForGroup("B+",s.today())==2,"Failed issue leaves stock unchanged");
        s.selectRole("PATIENT"); s.selectPatient(p.getPersonId());
        check(s.requests().size()==2 && s.requests().stream().allMatch(q->q.getPatientId().equals(p.getPersonId())),"Patient sees own requests");
        denied(()->s.issueBlood(shortage.getTransactionId()));
        BloodBankService reloaded=new BloodBankService(files.loadData(),files,clock);
        reloaded.selectRole("HOSPITAL");
        check(reloaded.patients().size()==3,"Patients persist");
        check(reloaded.patients().stream().filter(q->q.getPersonId().equals(p.getPersonId())).findFirst().orElseThrow().getStatus().equals("COMPLETED"),"Patient completion restored from request history");
        check(reloaded.patientsAwaitingBlood().stream().noneMatch(q->q.getPersonId().equals(p.getPersonId())),"Completed patient excluded after restart");
        denied(()->reloaded.requestBlood(p.getPersonId(),"HIGH"));
        BloodRequest completed=reloaded.requests().stream().filter(q->q.getTransactionId().equals(r.getTransactionId())).findFirst().orElseThrow();
        check(completed.getStatus().equals("COMPLETED") && completed.getIssuedUnitIds().size()==3,"Completion and unit IDs persist");
        check(reloaded.ownInventory().getStockForGroup("B+",reloaded.today())==2,"Deduction persists");
        check(reloaded.ownInventory().getBloodUnits().stream().filter(u->u.getStatus().equals("ISSUED")).allMatch(u->p.getPersonId().equals(u.getIssuedTo())),"Recipients persist");
        SystemState batches=DemoData.create();
        LocalDate today=LocalDate.now(clock);
        batches.facilities.get("HOSP001").getInventory().addBloodUnit(new BloodUnit("BATCH","A+",5,today,today.plusDays(10),"AVAILABLE","OPENING_STOCK"));
        BloodBankService b=new BloodBankService(batches,new FileManager(path.resolve("batch")),clock);
        b.selectRole("HOSPITAL"); BloodRequest br=b.requestBlood("PAT001","LOW"); b.issueBlood(br.getTransactionId());
        check(b.ownInventory().getStockForGroup("A+",today)==3,"Partial batch deducted");
        check(b.ownInventory().getBloodUnits().stream().filter(u->u.getStatus().equals("ISSUED")).mapToInt(BloodUnit::getQuantity).sum()==2,"Split batch issue retained");
        SystemState expired=DemoData.create();
        expired.facilities.get("HOSP001").getInventory().addBloodUnit(new BloodUnit("OLD","A+",5,today.minusDays(42),today,"AVAILABLE","OPENING_STOCK"));
        BloodBankService e=new BloodBankService(expired,new FileManager(path.resolve("expired")),clock);
        e.selectRole("HOSPITAL"); BloodRequest er=e.requestBlood("PAT001","HIGH"); denied(()->e.issueBlood(er.getTransactionId()));
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        new Menu(reloaded,new StringReader("4\n3\n2\nHIGH\n7\n2\n2\n3\n1\n5\n5\n"),new PrintStream(out)).displayMainMenu();
        String text=out.toString();
        check(text.contains("Choose patient number") && text.contains("Requesting 3 units of B+"),"Patient list and automatic quantity");
        check(!text.contains("Check Local Inventory") && !text.contains("Units required:"),"No redundant inventory option or request quantity prompt");
        check(text.contains("Choose request number"),"Request status uses selection");
        ByteArrayOutputStream choices = new ByteArrayOutputStream();
        new Menu(reloaded,new StringReader("4\n3\n99\n7\n5\n"),new PrintStream(choices)).displayMainMenu();
        check(!choices.toString().contains("New patient") && choices.toString().contains("Waiting patient"), "Request menu omits completed patient name");
        ByteArrayOutputStream empty = new ByteArrayOutputStream();
        new Menu(b,new StringReader("4\n3\n7\n5\n"),new PrintStream(empty)).displayMainMenu();
        check(empty.toString().contains("No patients awaiting blood"), "All-completed request list handled");
        try(var stream=Files.walk(path)) { for(Path item:stream.sorted(Comparator.reverseOrder()).toList()) Files.delete(item); }
        System.out.println("PASS: "+checks+" patient workflow assertions");
    }
}
