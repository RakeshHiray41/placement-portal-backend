package com.placement.placement.controller;

import com.placement.placement.dto.EmailRequest;
import com.placement.placement.entity.Company;
import com.placement.placement.entity.Placement;
import com.placement.placement.entity.Student;
import com.placement.placement.repository.CompanyRepository;
import com.placement.placement.repository.StudentRepository;
import com.placement.placement.repository.PlacementRepository;
import com.placement.placement.service.EmailService;
import com.placement.placement.service.GeminiService;
import com.placement.placement.service.StorageService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController
{

    private final StudentRepository studentRepo;
    private final CompanyRepository companyRepo;
    private final StorageService storageService;
    private final EmailService emailService;
    private final GeminiService geminiService;

    public AdminController(StudentRepository s, CompanyRepository c, StorageService ss, EmailService e, GeminiService g)
    {
        this.studentRepo = s;
        this.companyRepo = c;
        this.storageService = ss;
        this.emailService = e;
        this.geminiService = g;
    }

    @Autowired
    private PlacementRepository placementRepo;

    // ================= VALIDATION HELPERS =================
    private static final String EMAIL_REGEX = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    private void requireValidEmail(String value, String fieldName) {
        requireNotBlank(value, fieldName);
        if (!value.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Valid " + fieldName + " is required");
        }
    }

    // ================= STUDENT =================


    @PostMapping(
            value = "/students",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Student createStudent(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String resumeUrl,
            @RequestParam(required = false) MultipartFile photoFile
    ) throws IOException {

        requireNotBlank(fullName, "Full name");
        requireValidEmail(email, "Email");

        Student s = new Student();
        s.setFullName(fullName);
        s.setEmail(email);
        s.setPhone(phone);
        s.setResumeUrl(resumeUrl);

        if (photoFile != null && !photoFile.isEmpty()) {
            s.setPhotoUrl(storageService.store(photoFile));
        }

        return studentRepo.save(s);
    }

    @GetMapping("/students")
    public List<Student> getAllStudents() {
        return studentRepo.findAll();
    }

    @GetMapping("/students/{id}")
    public Student getStudent(@PathVariable Long id) {
        return studentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }

    @PutMapping(
            value = "/students/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Student updateStudent(
            @PathVariable Long id,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String resumeUrl,
            @RequestParam(required = false) MultipartFile photoFile
    ) throws IOException {

        requireNotBlank(fullName, "Full name");
        requireValidEmail(email, "Email");

        Student s = studentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        s.setFullName(fullName);
        s.setEmail(email);
        s.setPhone(phone);
        s.setResumeUrl(resumeUrl);

        if (photoFile != null && !photoFile.isEmpty()) {
            // FIX: previously the old photo file was left on disk forever
            // (orphaned) every time a new one was uploaded. Now the old
            // one is deleted right after the new one is successfully stored.
            String oldPhotoUrl = s.getPhotoUrl();
            s.setPhotoUrl(storageService.store(photoFile));
            storageService.delete(oldPhotoUrl);
        }

        return studentRepo.save(s);
    }

    // FIX: deleting a student that already had placement records used to
    // fail with a database foreign-key error, because those placement
    // rows still pointed at this student. Now the related placements are
    // removed first, then the student itself.
    @DeleteMapping("/students/{id}")
    public ResponseEntity<String> deleteStudent(@PathVariable Long id) {
        try {
            placementRepo.deleteByStudentId(id);
            studentRepo.deleteById(id);
            return ResponseEntity.ok("Student deleted with id: " + id);
        } catch (EmptyResultDataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found with id: " + id);
        }
    }



    // ================= COMPANY =================


    @PostMapping("/companies")
    public Company createCompany(
            @RequestParam String name,
            @RequestParam(required = false) String website,
            @RequestParam(required = false) String contactEmail,
            @RequestParam(required = false) MultipartFile logo
    ) throws IOException {

        requireNotBlank(name, "Company name");
        if (contactEmail != null && !contactEmail.isBlank()) {
            requireValidEmail(contactEmail, "Contact email");
        }

        Company c = new Company();
        c.setName(name);
        c.setWebsite(website);
        c.setContactEmail(contactEmail);

        if (logo != null) {
            c.setLogoUrl(storageService.store(logo));
        }

        return companyRepo.save(c);
    }




    @GetMapping("/companies")
    public List<Company> getAllCompanies() {
        return companyRepo.findAll();
    }

    @GetMapping("/companies/{id}")
    public Company getCompany(@PathVariable Long id) {
        return companyRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
    }


    @PutMapping("/companies/{id}")
    public Company updateCompany(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false) String website,
            @RequestParam(required = false) String contactEmail,
            @RequestParam(required = false) MultipartFile logo
    ) throws IOException {

        requireNotBlank(name, "Company name");
        if (contactEmail != null && !contactEmail.isBlank()) {
            requireValidEmail(contactEmail, "Contact email");
        }

        Company c = companyRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        c.setName(name);
        c.setWebsite(website);
        c.setContactEmail(contactEmail);

        if (logo != null) {
            // FIX: same orphaned-file issue as student photos -- delete the old
            // logo from disk once the new one is stored successfully.
            String oldLogoUrl = c.getLogoUrl();
            c.setLogoUrl(storageService.store(logo));
            storageService.delete(oldLogoUrl);
        }

        return companyRepo.save(c);
    }


    // FIX: same relation issue as students -- delete this company's
    // placement records first, then the company itself.
    @DeleteMapping("/companies/{id}")
    public ResponseEntity<String> deleteCompany(@PathVariable Long id) {
        try {
            placementRepo.deleteByCompanyId(id);
            companyRepo.deleteById(id);
            return ResponseEntity.ok("Company deleted with id: " + id);
        } catch (EmptyResultDataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Company not found with id: " + id);
        }
    }

    @PostMapping("/companies/{id}/promote")
    public String promoteCompany(@PathVariable Long id) throws MessagingException {
        Company c = companyRepo.findById(id).orElseThrow(() -> new RuntimeException("Company not found"));

        if (c.getContactEmail() == null || c.getContactEmail().isBlank()) {
            throw new IllegalArgumentException("This company has no contact email on file, so a promotion email cannot be sent.");
        }

        String aiText = geminiService.generatePromotionalContent(c.getName());
        emailService.sendEmail(c.getContactEmail(), "Partnership Invitation", aiText);
        return "Email sent to " + c.getContactEmail();
    }

    // ================= PLACEMENT =================

    @PostMapping("/placements")
    public Placement createPlacement(@RequestParam Long studentId,
                                     @RequestParam Long companyId,
                                     @RequestParam String role,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate placedDate) {

        requireNotBlank(role, "Role");

        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Company company = companyRepo.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Placement p = new Placement();
        p.setStudent(student);
        p.setCompany(company);
        p.setRole(role);
        p.setPlacedDate(placedDate);

        return placementRepo.save(p);
    }

    @GetMapping("/placements")
    public List<Placement> getAllPlacements() {
        return placementRepo.findAll();
    }

    @GetMapping("/placements/{id}")
    public Placement getPlacementById(@PathVariable Long id) {
        return placementRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Placement not found"));
    }

    @PutMapping("/placements/{id}")
    public Placement updatePlacement(@PathVariable Long id,
                                     @RequestParam Long studentId,
                                     @RequestParam Long companyId,
                                     @RequestParam String role,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate placedDate) {

        requireNotBlank(role, "Role");

        Placement p = placementRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Placement not found"));

        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Company company = companyRepo.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        p.setStudent(student);
        p.setCompany(company);
        p.setRole(role);
        p.setPlacedDate(placedDate);

        return placementRepo.save(p);
    }

    @DeleteMapping("/placements/{id}")
    public ResponseEntity<String> deletePlacement(@PathVariable Long id) {
        try {
            placementRepo.deleteById(id);
            return ResponseEntity.ok("Placement deleted with id: " + id);
        } catch (EmptyResultDataAccessException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Placement not found with id: " + id);
        }
    }



    @GetMapping("/test")
    public String testGemini(@RequestParam String company) {
        String[] parts = company.split(":::", 2);
        String companyName = parts[0];
        String extraInstructions = parts.length > 1 ? parts[1] : null;
        return geminiService.generatePromotionalContent(companyName, extraInstructions);
    }



    @PostMapping("/send-email")
    public String sendCustomEmail(@RequestBody Map<String, String> data) throws MessagingException {

        String to = data.get("to");
        String subject = data.get("subject");
        String body = data.get("body");

        requireValidEmail(to, "Recipient email");
        requireNotBlank(subject, "Subject");
        requireNotBlank(body, "Body");

        emailService.sendEmail(to, subject, body);

        return "Email sent to " + to;
    }

}