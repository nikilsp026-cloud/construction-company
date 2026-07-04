package com.construction;

import com.construction.config.AppProperties;
import com.construction.entity.*;
import com.construction.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ConstructionServiceRepository constructionServiceRepository;
    private final ProjectRepository projectRepository;
    private final TestimonialRepository testimonialRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final WebsiteSettingRepository websiteSettingRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        seedRoles();
        seedAdminUser();
        seedServices();
        seedProjects();
        seedTestimonials();
        seedTeamMembers();
        seedWebsiteSettings();
    }

    // -------------------------------------------------------------------------
    // 1. Roles
    // -------------------------------------------------------------------------
    private void seedRoles() {
        seedRoleIfAbsent("ROLE_ADMIN");
        seedRoleIfAbsent("ROLE_EMPLOYEE");
        log.info("Roles seeded.");
    }

    private void seedRoleIfAbsent(String name) {
        if (!roleRepository.existsByName(name)) {
            Role role = new Role();
            role.setName(name);
            roleRepository.save(role);
        }
    }

    // -------------------------------------------------------------------------
    // 2. Admin user
    // -------------------------------------------------------------------------
    private void seedAdminUser() {
        String email = appProperties.getAdmin().getEmail();
        if (userRepository.existsByEmail(email)) {
            return;
        }

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);

        User admin = new User();
        admin.setUsername(appProperties.getAdmin().getUsername());
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(appProperties.getAdmin().getPassword()));
        admin.setFullName(appProperties.getAdmin().getFullName());
        admin.setEnabled(true);
        admin.setRoles(roles);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());

        userRepository.save(admin);
        log.info("Admin user seeded: {}", email);
    }

    // -------------------------------------------------------------------------
    // 3. Construction Services
    // -------------------------------------------------------------------------
    private void seedServices() {
        if (constructionServiceRepository.count() > 0) {
            return;
        }

        List<Object[]> services = List.of(
                new Object[]{"General Contracting",
                        "Full-service general contracting solutions tailored to your project needs. We manage every phase from planning through completion, ensuring quality craftsmanship and timely delivery.",
                        "fas fa-hard-hat"},
                new Object[]{"Commercial Construction",
                        "State-of-the-art commercial buildings designed for performance and longevity. From office towers to retail complexes, we bring your commercial vision to life.",
                        "fas fa-building"},
                new Object[]{"Residential Construction",
                        "Custom homes and residential communities built with care and precision. We work closely with homeowners to create spaces that reflect their lifestyle and aspirations.",
                        "fas fa-home"},
                new Object[]{"Renovation & Remodeling",
                        "Transform your existing space with our expert renovation and remodeling services. We breathe new life into homes and commercial properties while minimising disruption.",
                        "fas fa-tools"},
                new Object[]{"Interior Design",
                        "Elegant, functional interiors crafted by our talented design team. We blend aesthetics with practicality to create environments that inspire and impress.",
                        "fas fa-couch"},
                new Object[]{"Project Management",
                        "End-to-end project management that keeps your build on schedule and within budget. Our certified managers coordinate every stakeholder so nothing falls through the cracks.",
                        "fas fa-project-diagram"}
        );

        int order = 1;
        for (Object[] row : services) {
            ConstructionService service = new ConstructionService();
            service.setTitle((String) row[0]);
            service.setDescription((String) row[1]);
            service.setIcon((String) row[2]);
            service.setStatus(ConstructionService.ServiceStatus.ACTIVE);
            service.setDisplayOrder(order++);
            service.setCreatedAt(LocalDateTime.now());
            service.setUpdatedAt(LocalDateTime.now());
            constructionServiceRepository.save(service);
        }
        log.info("Construction services seeded.");
    }

    // -------------------------------------------------------------------------
    // 4. Projects
    // -------------------------------------------------------------------------
    private void seedProjects() {
        if (projectRepository.count() > 0) {
            return;
        }

        Project p1 = new Project();
        p1.setName("Downtown Office Tower");
        p1.setDescription("A 28-storey Class-A office tower in the heart of the business district, featuring sustainable design, smart-building technology, and LEED Gold certification.");
        p1.setClientName("Apex Corporate Holdings");
        p1.setLocation("Downtown Financial District, Builder City");
        p1.setStartDate(LocalDate.of(2021, 3, 1));
        p1.setCompletionDate(LocalDate.of(2023, 11, 30));
        p1.setStatus(Project.ProjectStatus.COMPLETED);
        p1.setBudget(new BigDecimal("42500000.00"));
        p1.setCategory("Commercial");
        p1.setFeatured(true);
        p1.setCompletionPercentage(100);
        p1.setCreatedAt(LocalDateTime.now());
        p1.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(p1);

        Project p2 = new Project();
        p2.setName("Green Valley Residences");
        p2.setDescription("A master-planned residential community of 120 eco-friendly townhomes and single-family dwellings with parks, walking trails, and solar-ready infrastructure.");
        p2.setClientName("Green Valley Developers LLC");
        p2.setLocation("Green Valley Suburbs, Builder City");
        p2.setStartDate(LocalDate.of(2023, 6, 15));
        p2.setCompletionDate(LocalDate.of(2025, 12, 31));
        p2.setStatus(Project.ProjectStatus.ONGOING);
        p2.setBudget(new BigDecimal("18750000.00"));
        p2.setCategory("Residential");
        p2.setFeatured(true);
        p2.setCompletionPercentage(65);
        p2.setCreatedAt(LocalDateTime.now());
        p2.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(p2);

        Project p3 = new Project();
        p3.setName("Harbor Bridge Renovation");
        p3.setDescription("Comprehensive structural rehabilitation and aesthetic upgrade of the century-old Harbor Bridge, including new pedestrian walkways, lighting, and seismic reinforcement.");
        p3.setClientName("City of Builder City — Department of Infrastructure");
        p3.setLocation("Harbor District, Builder City");
        p3.setStartDate(LocalDate.of(2022, 1, 10));
        p3.setCompletionDate(LocalDate.of(2024, 4, 30));
        p3.setStatus(Project.ProjectStatus.COMPLETED);
        p3.setBudget(new BigDecimal("9300000.00"));
        p3.setCategory("Infrastructure");
        p3.setFeatured(false);
        p3.setCompletionPercentage(100);
        p3.setCreatedAt(LocalDateTime.now());
        p3.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(p3);

        Project p4 = new Project();
        p4.setName("Riverside Mall");
        p4.setDescription("A premium two-level retail and entertainment destination along the riverfront, featuring 80+ shops, a food court, multiplex cinema, and riverside promenade.");
        p4.setClientName("Riverside Retail Group");
        p4.setLocation("Riverside Precinct, Builder City");
        p4.setStartDate(LocalDate.of(2025, 9, 1));
        p4.setCompletionDate(LocalDate.of(2027, 6, 30));
        p4.setStatus(Project.ProjectStatus.UPCOMING);
        p4.setBudget(new BigDecimal("55000000.00"));
        p4.setCategory("Commercial");
        p4.setFeatured(true);
        p4.setCompletionPercentage(0);
        p4.setCreatedAt(LocalDateTime.now());
        p4.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(p4);

        log.info("Projects seeded.");
    }

    // -------------------------------------------------------------------------
    // 5. Testimonials
    // -------------------------------------------------------------------------
    private void seedTestimonials() {
        if (testimonialRepository.count() > 0) {
            return;
        }

        Testimonial t1 = new Testimonial();
        t1.setCustomerName("Jonathan Mercer");
        t1.setCompany("Apex Corporate Holdings");
        t1.setRating(5);
        t1.setComment("BuildMaster delivered our office tower on time and under budget. The attention to detail was exceptional — every floor was finished to the highest standard. We could not have asked for a better construction partner.");
        t1.setStatus(Testimonial.TestimonialStatus.ACTIVE);
        t1.setCreatedAt(LocalDateTime.now());
        testimonialRepository.save(t1);

        Testimonial t2 = new Testimonial();
        t2.setCustomerName("Sarah Whitfield");
        t2.setCompany("Green Valley Developers LLC");
        t2.setRating(5);
        t2.setComment("The team at BuildMaster transformed our vision for a sustainable community into reality. Their project managers kept us informed at every stage, and the quality of the homes is outstanding.");
        t2.setStatus(Testimonial.TestimonialStatus.ACTIVE);
        t2.setCreatedAt(LocalDateTime.now());
        testimonialRepository.save(t2);

        Testimonial t3 = new Testimonial();
        t3.setCustomerName("Commissioner David Park");
        t3.setCompany("City of Builder City");
        t3.setRating(5);
        t3.setComment("Renovating a historic bridge without disrupting the city was a massive challenge. BuildMaster handled every complexity with professionalism and skill. The finished bridge is a point of civic pride.");
        t3.setStatus(Testimonial.TestimonialStatus.ACTIVE);
        t3.setCreatedAt(LocalDateTime.now());
        testimonialRepository.save(t3);

        log.info("Testimonials seeded.");
    }

    // -------------------------------------------------------------------------
    // 6. Team Members
    // -------------------------------------------------------------------------
    private void seedTeamMembers() {
        if (teamMemberRepository.count() > 0) {
            return;
        }

        TeamMember ceo = new TeamMember();
        ceo.setName("Robert J. Harrison");
        ceo.setDesignation("Chief Executive Officer");
        ceo.setDescription("Robert brings over 30 years of construction industry experience, leading BuildMaster from a small local contractor to a nationally recognised firm. His vision centres on quality, integrity, and sustainable building practices.");
        ceo.setDisplayOrder(1);
        ceo.setActive(true);
        ceo.setCreatedAt(LocalDateTime.now());
        teamMemberRepository.save(ceo);

        TeamMember architect = new TeamMember();
        architect.setName("Dr. Priya Nair");
        architect.setDesignation("Lead Architect");
        architect.setDescription("Priya holds a doctorate in Structural Architecture and has designed award-winning commercial and residential projects across the country. She champions innovative design that balances beauty with engineered precision.");
        architect.setDisplayOrder(2);
        architect.setActive(true);
        architect.setCreatedAt(LocalDateTime.now());
        teamMemberRepository.save(architect);

        TeamMember pm = new TeamMember();
        pm.setName("Marcus T. Williams");
        pm.setDesignation("Project Manager");
        pm.setDescription("A PMP-certified project manager with 15 years of on-site and strategic experience, Marcus ensures every project is delivered on schedule and within scope. His proactive communication keeps clients confident at every milestone.");
        pm.setDisplayOrder(3);
        pm.setActive(true);
        pm.setCreatedAt(LocalDateTime.now());
        teamMemberRepository.save(pm);

        TeamMember safety = new TeamMember();
        safety.setName("Linda Chen");
        safety.setDesignation("Safety Officer");
        safety.setDescription("Linda oversees all health and safety protocols across BuildMaster's active sites. With certifications in OSHA and ISO 45001, she maintains BuildMaster's exemplary safety record and fosters a zero-incident culture.");
        safety.setDisplayOrder(4);
        safety.setActive(true);
        safety.setCreatedAt(LocalDateTime.now());
        teamMemberRepository.save(safety);

        log.info("Team members seeded.");
    }

    // -------------------------------------------------------------------------
    // 7. Website Settings
    // -------------------------------------------------------------------------
    private void seedWebsiteSettings() {
        if (websiteSettingRepository.count() > 0) {
            return;
        }

        List<String[]> settings = List.of(
                new String[]{"site_name",        "KV Construction",                                                        "text"},
                new String[]{"site_tagline",      "Building Dreams, Delivering Excellence",                                          "text"},
                new String[]{"contact_email",     "info@buildmaster.com",                                                            "text"},
                new String[]{"contact_phone",     "+1 555 123-4567",                                                                 "text"},
                new String[]{"contact_address",   "123 Construction Ave, Builder City, BC 12345",                                    "textarea"},
                new String[]{"facebook_url",      "#",                                                                               "url"},
                new String[]{"linkedin_url",      "#",                                                                               "url"},
                new String[]{"twitter_url",       "#",                                                                               "url"},
                new String[]{"instagram_url",     "#",                                                                               "url"},
                new String[]{"about_text",        "KV Construction has been shaping skylines and communities for over three decades. We combine time-tested craftsmanship with modern technology to deliver projects that stand the test of time.",
                                                                                                                                     "textarea"},
                new String[]{"hero_title",        "KV Construction",                                                        "text"},
                new String[]{"hero_subtitle",     "Quality Construction, Excellence Delivered",                                      "text"}
        );

        for (String[] row : settings) {
            if (!websiteSettingRepository.existsBySettingKey(row[0])) {
                WebsiteSetting setting = new WebsiteSetting();
                setting.setSettingKey(row[0]);
                setting.setSettingValue(row[1]);
                setting.setSettingType(row[2]);
                websiteSettingRepository.save(setting);
            }
        }
        log.info("Website settings seeded.");
    }
}
