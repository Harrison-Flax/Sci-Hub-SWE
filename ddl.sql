create table author
(
    id                     bigint auto_increment
        primary key,
    author_name            varchar(255) null,
    first_name             varchar(255) null,
    last_name              varchar(255) null,
    middle_initial         varchar(255) null,
    affiliation            varchar(255) null,
    title                  varchar(255) null,
    email                  varchar(255) null,
    mailing_address        varchar(255) null,
    phone_number           varchar(255) null,
    research_fields        varchar(255) null,
    highest_degree         varchar(255) null,
    level                  varchar(255) null,
    homepage               varchar(255) null,
    avatar                 varchar(255) null,
    create_time            varchar(255) null,
    is_active              varchar(255) null,
    rating                 double       not null,
    rating_count           bigint       not null,
    recommend_rating       double       not null,
    recommend_rating_count bigint       not null
);

create table author_paper
(
    author_id bigint null,
    paper_id  bigint null
);

create table course
(
    id             bigint auto_increment
        primary key,
    is_active      tinyint(1) default 0 not null,
    course_id      varchar(255)         null,
    name           varchar(255)         null,
    description    varchar(255)         null,
    prerequisite   varchar(255)         null,
    start_semester varchar(255)         null,
    start_year     varchar(255)         null,
    end_semester   varchar(255)         null,
    end_year       varchar(255)         null
);

create table file
(
    id        bigint auto_increment
        primary key,
    file_name varchar(255) null,
    path      varchar(255) null,
    timestamp datetime(6)  null
);

create table operation_log
(
    id            bigint auto_increment
        primary key,
    logger        varchar(255) null,
    log_key       varchar(255) null,
    channel       varchar(255) null,
    user_id       bigint       null,
    date_time     varchar(255) null,
    timestamp     bigint       null,
    ip            varchar(255) null,
    route_pattern varchar(255) null,
    action_method varchar(255) null,
    controller    varchar(255) null,
    comment       varchar(255) null
);

create table organization
(
    id                   bigint auto_increment
        primary key,
    organization_name    varchar(255) null,
    address              varchar(255) null,
    focuses              varchar(255) null,
    url                  varchar(255) null,
    organization_logo    varchar(255) null,
    number_of_employees  int          not null,
    organization_history varchar(255) null,
    street_address1      varchar(255) null,
    street_address2      varchar(255) null,
    city                 varchar(255) null,
    state                varchar(255) null,
    zip_code             int          not null,
    short_description    varchar(255) null,
    long_description     varchar(255) null,
    fields               varchar(255) null,
    contact_person_name  varchar(255) null,
    contact_person_email varchar(255) null,
    contact_person_phone bigint       null,
    registrar_id         bigint       null
);

create table paper
(
    id                  bigint auto_increment
        primary key,
    title               varchar(255) null,
    book_title          varchar(255) null,
    editor              varchar(255) null,
    abstract_text       varchar(255) null,
    publication_type    varchar(255) null,
    publication_channel varchar(255) null,
    date                varchar(255) null,
    year                varchar(255) null,
    month               varchar(255) null,
    url                 varchar(255) null,
    publisher           varchar(255) null,
    address             varchar(255) null,
    isbn                varchar(255) null,
    series              varchar(255) null,
    school              varchar(255) null,
    chapter             varchar(255) null,
    volume              varchar(255) null,
    number              varchar(255) null,
    pages               varchar(255) null,
    all_authors_string  varchar(255) null
);

create table project
(
    id                                     bigint auto_increment
        primary key,
    is_active                              varchar(255)         null,
    parent_project_id                      bigint               null,
    is_popular                             tinyint(1) default 0 not null,
    popular_ranking                        bigint               not null,
    authentication                         varchar(255)         null,
    access_times                           bigint               not null,
    next_image_index                       int                  null,
    title                                  varchar(255)         null,
    technology                             varchar(255)         null,
    pdf                                    varchar(255)         null,
    image_url                              varchar(255)         null,
    goals                                  varchar(255)         null,
    video_url                              varchar(255)         null,
    github_url                             varchar(255)         null,
    team_page_url                          varchar(255)         null,
    location                               varchar(255)         null,
    description                            varchar(255)         null,
    start_date                             varchar(255)         null,
    end_date                               varchar(255)         null,
    principal_investigator_id              bigint               null,
    sponsor_contact_id                     bigint               null,
    principal_investigator_organization_id bigint               null,
    sponsor_organization_id                bigint               null,
    constraint fk_project_principal_investigator_organization_id
        foreign key (principal_investigator_organization_id) references organization (id),
    constraint fk_project_sponsor_organization_id
        foreign key (sponsor_organization_id) references organization (id)
);

create index ix_project_principal_investigator_id
    on project (principal_investigator_id);

create index ix_project_principal_investigator_organization_id
    on project (principal_investigator_organization_id);

create index ix_project_sponsor_contact_id
    on project (sponsor_contact_id);

create index ix_project_sponsor_organization_id
    on project (sponsor_organization_id);

create table reviewer
(
    id                     bigint auto_increment
        primary key,
    reviewer_name          varchar(255) null,
    password               varchar(255) null,
    first_name             varchar(255) null,
    last_name              varchar(255) null,
    middle_initial         varchar(255) null,
    affiliation            varchar(255) null,
    title                  varchar(255) null,
    email                  varchar(255) null,
    mailing_address        varchar(255) null,
    phone_number           varchar(255) null,
    research_fields        varchar(255) null,
    highest_degree         varchar(255) null,
    level                  varchar(255) null,
    homepage               varchar(255) null,
    avatar                 varchar(255) null,
    rating                 double       not null,
    rating_count           bigint       not null,
    recommend_rating       double       not null,
    recommend_rating_count bigint       not null,
    create_time            varchar(255) null,
    is_active              varchar(255) null
);

create table user
(
    id                       bigint auto_increment
        primary key,
    user_name                varchar(255)         null,
    password                 varchar(255)         null,
    first_name               varchar(255)         null,
    last_name                varchar(255)         null,
    middle_initial           varchar(255)         null,
    organization             varchar(255)         null,
    email                    varchar(255)         null,
    mailing_address          varchar(255)         null,
    phone_number             varchar(255)         null,
    level                    varchar(255)         null,
    rating                   double               not null,
    rating_count             bigint               not null,
    recommend_rating         double               not null,
    recommend_rating_count   bigint               not null,
    homepage                 varchar(255)         null,
    avatar                   varchar(255)         null,
    service_provider         tinyint(1) default 0 not null,
    expertises               varchar(255)         null,
    categories               varchar(255)         null,
    detail                   varchar(255)         null,
    user_type                int                  null,
    service_execution_counts bigint               not null,
    service_user             tinyint(1) default 0 not null,
    create_time              varchar(255)         null,
    is_active                varchar(255)         null,
    project_zone_id          bigint               null,
    unread_mention           tinyint(1) default 0 not null,
    constraint fk_user_project_zone_id
        foreign key (project_zone_id) references project (id)
);

create table bug_report
(
    id               bigint auto_increment
        primary key,
    title            varchar(255) null,
    description      varchar(255) null,
    solved           int          not null,
    create_time      datetime(6)  null,
    solve_time       datetime(6)  null,
    long_description varchar(255) null,
    reporter_id      bigint       null,
    fixer_id         bigint       null,
    constraint fk_bug_report_fixer_id
        foreign key (fixer_id) references user (id),
    constraint fk_bug_report_reporter_id
        foreign key (reporter_id) references user (id)
);

create index ix_bug_report_fixer_id
    on bug_report (fixer_id);

create index ix_bug_report_reporter_id
    on bug_report (reporter_id);

create table challenge
(
    id                     bigint auto_increment
        primary key,
    challenge_title        varchar(255) null,
    short_description      varchar(255) null,
    long_description       varchar(255) null,
    required_expertise     varchar(255) null,
    preferred_expertise    varchar(255) null,
    preferred_time         varchar(255) null,
    tech                   varchar(255) null,
    time                   varchar(255) null,
    budget                 varchar(255) null,
    location               varchar(255) null,
    organizations          varchar(255) null,
    status                 varchar(255) null,
    min_budget             int          not null,
    max_budget             int          not null,
    challenge_pdf          varchar(255) null,
    challenge_image        varchar(255) null,
    rating                 double       not null,
    rating_count           bigint       not null,
    recommend_rating       double       not null,
    recommend_rating_count bigint       not null,
    homepage               varchar(255) null,
    avatar                 varchar(255) null,
    created_time           varchar(255) null,
    is_active              varchar(255) null,
    challenge_publisher_id bigint       null,
    number_of_applicants   int          not null,
    constraint fk_challenge_challenge_publisher_id
        foreign key (challenge_publisher_id) references user (id)
);

create index ix_challenge_challenge_publisher_id
    on challenge (challenge_publisher_id);

create table challenge_application
(
    id                     bigint auto_increment
        primary key,
    challenge_id           bigint       null,
    applicant_id           bigint       null,
    apply_headline         varchar(255) null,
    apply_cover_letter     varchar(255) null,
    apply_description      varchar(255) null,
    rating                 double       not null,
    rating_count           bigint       not null,
    recommend_rating       double       not null,
    recommend_rating_count bigint       not null,
    homepage               varchar(255) null,
    avatar                 varchar(255) null,
    created_time           varchar(255) null,
    is_active              varchar(255) null,
    constraint fk_challenge_application_applicant_id
        foreign key (applicant_id) references user (id),
    constraint fk_challenge_application_challenge_id
        foreign key (challenge_id) references challenge (id)
);

create index ix_challenge_application_applicant_id
    on challenge_application (applicant_id);

create index ix_challenge_application_challenge_id
    on challenge_application (challenge_id);

create table followers
(
    userid     bigint not null,
    followerid bigint not null,
    primary key (userid, followerid),
    constraint fk_followers_user_1
        foreign key (userid) references user (id),
    constraint fk_followers_user_2
        foreign key (followerid) references user (id)
);

create index ix_followers_user_1
    on followers (userid);

create index ix_followers_user_2
    on followers (followerid);

create table friendrequests
(
    userid   bigint not null,
    senderid bigint not null,
    primary key (userid, senderid),
    constraint fk_friendrequests_user_1
        foreign key (userid) references user (id),
    constraint fk_friendrequests_user_2
        foreign key (senderid) references user (id)
);

create index ix_friendrequests_user_1
    on friendrequests (userid);

create index ix_friendrequests_user_2
    on friendrequests (senderid);

create table friendship
(
    useraid bigint not null,
    userbid bigint not null,
    primary key (useraid, userbid),
    constraint fk_friendship_user_1
        foreign key (useraid) references user (id),
    constraint fk_friendship_user_2
        foreign key (userbid) references user (id)
);

create index ix_friendship_user_1
    on friendship (useraid);

create index ix_friendship_user_2
    on friendship (userbid);

create table job
(
    id                       bigint auto_increment
        primary key,
    is_active                varchar(255) null,
    pdf                      varchar(255) null,
    jobtxt                   varchar(255) null,
    status                   varchar(255) null,
    title                    varchar(255) null,
    goals                    varchar(255) null,
    min_salary               int          not null,
    max_salary               int          not null,
    short_description        varchar(255) null,
    long_description         varchar(255) null,
    fields                   varchar(255) null,
    publish_date             varchar(255) null,
    publish_year             varchar(255) null,
    publish_month            varchar(255) null,
    image_url                varchar(255) null,
    url                      varchar(255) null,
    organization             varchar(255) null,
    location                 varchar(255) null,
    required_expertise       varchar(255) null,
    preferred_expertise      varchar(255) null,
    number_of_positions      varchar(255) null,
    expected_start_date      varchar(255) null,
    job_publisher_id         bigint       null,
    contact_person_name      varchar(255) null,
    contact_person_email     varchar(255) null,
    contact_person_phone     varchar(255) null,
    salary_low               varchar(255) null,
    salary_high              varchar(255) null,
    minimum_degree           varchar(255) null,
    minimum_degree_in_fields varchar(255) null,
    type                     varchar(255) null,
    number_of_applicants     int          not null,
    constraint fk_job_job_publisher_id
        foreign key (job_publisher_id) references user (id)
);

create index ix_job_job_publisher_id
    on job (job_publisher_id);

create table job_application
(
    id                     bigint auto_increment
        primary key,
    job_id                 bigint       null,
    applicant_id           bigint       null,
    apply_headline         varchar(255) null,
    apply_cover_letter     varchar(255) null,
    referee1title          varchar(255) null,
    referee1last_name      varchar(255) null,
    referee1first_name     varchar(255) null,
    referee1email          varchar(255) null,
    referee1phone          varchar(255) null,
    referee2title          varchar(255) null,
    referee2last_name      varchar(255) null,
    referee2first_name     varchar(255) null,
    referee2email          varchar(255) null,
    referee2phone          varchar(255) null,
    referee3title          varchar(255) null,
    referee3last_name      varchar(255) null,
    referee3first_name     varchar(255) null,
    referee3email          varchar(255) null,
    referee3phone          varchar(255) null,
    rating                 double       not null,
    rating_count           bigint       not null,
    recommend_rating       double       not null,
    recommend_rating_count bigint       not null,
    homepage               varchar(255) null,
    avatar                 varchar(255) null,
    created_time           varchar(255) null,
    is_active              varchar(255) null,
    constraint fk_job_application_applicant_id
        foreign key (applicant_id) references user (id),
    constraint fk_job_application_job_id
        foreign key (job_id) references job (id)
);

create index ix_job_application_applicant_id
    on job_application (applicant_id);

create index ix_job_application_job_id
    on job_application (job_id);

create table mail
(
    id          bigint auto_increment
        primary key,
    title       varchar(255) null,
    content     varchar(255) null,
    timestamp   datetime(6)  null,
    sender_id   bigint       null,
    receiver_id bigint       null,
    constraint fk_mail_receiver_id
        foreign key (receiver_id) references user (id),
    constraint fk_mail_sender_id
        foreign key (sender_id) references user (id)
);

create index ix_mail_receiver_id
    on mail (receiver_id);

create index ix_mail_sender_id
    on mail (sender_id);

create table mail_file
(
    mail_id bigint not null,
    file_id bigint not null,
    primary key (mail_id, file_id),
    constraint fk_mail_file_file
        foreign key (file_id) references file (id),
    constraint fk_mail_file_mail
        foreign key (mail_id) references mail (id)
);

create index ix_mail_file_file
    on mail_file (file_id);

create index ix_mail_file_mail
    on mail_file (mail_id);

alter table project
    add constraint fk_project_principal_investigator_id
        foreign key (principal_investigator_id) references user (id);

alter table project
    add constraint fk_project_sponsor_contact_id
        foreign key (sponsor_contact_id) references user (id);

create table rajob
(
    id                     bigint auto_increment
        primary key,
    is_active              varchar(255) null,
    pdf                    varchar(255) null,
    status                 varchar(255) null,
    title                  varchar(255) null,
    goals                  varchar(255) null,
    min_salary             int          not null,
    max_salary             int          not null,
    ra_types               int          not null,
    short_description      varchar(255) null,
    long_description       varchar(255) null,
    fields                 varchar(255) null,
    publish_date           varchar(255) null,
    publish_year           varchar(255) null,
    publish_month          varchar(255) null,
    image_url              varchar(255) null,
    url                    varchar(255) null,
    organization           varchar(255) null,
    location               varchar(255) null,
    required_expertise     varchar(255) null,
    preferred_expertise    varchar(255) null,
    number_of_positions    varchar(255) null,
    expected_start_date    varchar(255) null,
    expected_time_duration varchar(255) null,
    rajob_publisher_id     bigint       null,
    number_of_applicants   int          not null,
    constraint fk_rajob_rajob_publisher_id
        foreign key (rajob_publisher_id) references user (id)
);

create index ix_rajob_rajob_publisher_id
    on rajob (rajob_publisher_id);

create table rajob_application
(
    id                     bigint auto_increment
        primary key,
    rajob_id               bigint       null,
    applicant_id           bigint       null,
    apply_headline         varchar(255) null,
    apply_cover_letter     varchar(255) null,
    referee1title          varchar(255) null,
    referee1last_name      varchar(255) null,
    referee1first_name     varchar(255) null,
    referee1email          varchar(255) null,
    referee1phone          varchar(255) null,
    referee2title          varchar(255) null,
    referee2last_name      varchar(255) null,
    referee2first_name     varchar(255) null,
    referee2email          varchar(255) null,
    referee2phone          varchar(255) null,
    referee3title          varchar(255) null,
    referee3last_name      varchar(255) null,
    referee3first_name     varchar(255) null,
    referee3email          varchar(255) null,
    referee3phone          varchar(255) null,
    rating                 double       not null,
    rating_count           bigint       not null,
    recommend_rating       double       not null,
    recommend_rating_count bigint       not null,
    homepage               varchar(255) null,
    avatar                 varchar(255) null,
    created_time           varchar(255) null,
    is_active              varchar(255) null,
    constraint fk_rajob_application_applicant_id
        foreign key (applicant_id) references user (id),
    constraint fk_rajob_application_rajob_id
        foreign key (rajob_id) references rajob (id)
);

create index ix_rajob_application_applicant_id
    on rajob_application (applicant_id);

create index ix_rajob_application_rajob_id
    on rajob_application (rajob_id);

create table researcher_info
(
    highest_degree  varchar(255) null,
    orcid           varchar(255) null,
    research_fields varchar(255) null,
    school          varchar(255) null,
    department      varchar(255) null,
    user_id         bigint       null,
    constraint uq_researcher_info_user_id
        unique (user_id),
    constraint fk_researcher_info_user_id
        foreign key (user_id) references user (id)
);

create table student_info
(
    id_number         varchar(255) null,
    student_year      varchar(255) null,
    student_type      varchar(255) null,
    major             varchar(255) null,
    first_enroll_date varchar(255) null,
    user_id           bigint       null,
    constraint uq_student_info_user_id
        unique (user_id),
    constraint fk_student_info_user_id
        foreign key (user_id) references user (id)
);

create table suggestion
(
    id               bigint auto_increment
        primary key,
    title            varchar(255) null,
    description      varchar(255) null,
    solved           int          not null,
    create_time      datetime(6)  null,
    solve_time       datetime(6)  null,
    long_description varchar(255) null,
    reporter_id      bigint       null,
    implementor_id   bigint       null,
    constraint fk_suggestion_implementor_id
        foreign key (implementor_id) references user (id),
    constraint fk_suggestion_reporter_id
        foreign key (reporter_id) references user (id)
);

create index ix_suggestion_implementor_id
    on suggestion (implementor_id);

create index ix_suggestion_reporter_id
    on suggestion (reporter_id);

create table tacandidate
(
    id              bigint auto_increment
        primary key,
    is_active       tinyint(1) default 0 not null,
    is_resume_sent  int                  not null,
    smu_id          varchar(255)         null,
    semester        varchar(255)         null,
    year            varchar(255)         null,
    status          varchar(255)         null,
    hours           int                  not null,
    courses         varchar(255)         null,
    ta_applicant_id bigint               null,
    preference      varchar(255)         null,
    unwanted        varchar(255)         null,
    comment         varchar(255)         null,
    constraint fk_tacandidate_ta_applicant_id
        foreign key (ta_applicant_id) references user (id)
);

create table course_taassignment
(
    id             bigint auto_increment
        primary key,
    course_id      bigint       null,
    ta_id          bigint       null,
    semester       varchar(255) null,
    year           varchar(255) null,
    approved_hours int          not null,
    f1approved     varchar(255) null,
    constraint fk_course_taassignment_course_id
        foreign key (course_id) references course (id),
    constraint fk_course_taassignment_ta_id
        foreign key (ta_id) references tacandidate (id)
);

create index ix_course_taassignment_course_id
    on course_taassignment (course_id);

create index ix_course_taassignment_ta_id
    on course_taassignment (ta_id);

create index ix_tacandidate_ta_applicant_id
    on tacandidate (ta_applicant_id);

create table tajob
(
    id                          bigint auto_increment
        primary key,
    is_active                   varchar(255) null,
    pdf                         varchar(255) null,
    status                      varchar(255) null,
    work_time                   int          not null,
    ta_job_semester_types       int          not null,
    ta_job_course_selections    int          not null,
    ta_courses_selection_hidden varchar(255) null,
    title                       varchar(255) null,
    goals                       varchar(255) null,
    min_salary                  int          not null,
    max_salary                  int          not null,
    ta_types                    int          not null,
    short_description           varchar(255) null,
    long_description            varchar(255) null,
    fields                      varchar(255) null,
    publish_date                varchar(255) null,
    publish_year                varchar(255) null,
    publish_month               varchar(255) null,
    image_url                   varchar(255) null,
    url                         varchar(255) null,
    organization                varchar(255) null,
    location                    varchar(255) null,
    required_expertise          varchar(255) null,
    preferred_expertise         varchar(255) null,
    number_of_positions         varchar(255) null,
    expected_start_date         varchar(255) null,
    expected_time_dutation      varchar(255) null,
    tajob_publisher_id          bigint       null,
    number_of_applicants        int          not null,
    constraint fk_tajob_tajob_publisher_id
        foreign key (tajob_publisher_id) references user (id)
);

create index ix_tajob_tajob_publisher_id
    on tajob (tajob_publisher_id);

create table tajob_application
(
    id                                  bigint auto_increment
        primary key,
    tajob_id                            bigint       null,
    applicant_id                        bigint       null,
    apply_date                          varchar(255) null,
    smu_id                              int          not null,
    ta_semester_types                   int          not null,
    ta_student_types                    int          not null,
    ta_student_admission_types          int          not null,
    ta_uscitizen                        int          not null,
    ta_native_language                  int          not null,
    ta_english_proficiency_test         int          not null,
    ta_english_proficiency_test_name    varchar(255) null,
    ta_english_proficiency_test_score   double       not null,
    ta_english_proficiency_test_date    varchar(255) null,
    gre_v                               double       not null,
    gre_q                               double       not null,
    gre_a                               double       not null,
    gre_date                            varchar(255) null,
    undergraduate_gpa                   double       not null,
    undergraduate_school                varchar(255) null,
    graduate_gpa                        double       not null,
    graduate_school                     varchar(255) null,
    class_rank_no_gpa                   int          not null,
    score_percentage_no_gpa             double       not null,
    grade_no_gpa                        double       not null,
    other_info_no_gpa                   varchar(255) null,
    enrolled_degree                     int          not null,
    enrolled_phd_degree                 int          not null,
    areas_research_interest1            varchar(255) null,
    areas_research_interest2            varchar(255) null,
    areas_research_interest3            varchar(255) null,
    areas_research_interest4            varchar(255) null,
    ra_smu                              int          not null,
    ra_smutime                          varchar(255) null,
    ra_smuadvisor_name                  varchar(255) null,
    ra_smuadvisor_email                 varchar(255) null,
    ta_smu                              int          not null,
    ta_smutime                          varchar(255) null,
    ta_smuadvisor_name                  varchar(255) null,
    ta_smuadvisor_email                 varchar(255) null,
    programming_language_cpp            int          not null,
    programming_language_java           int          not null,
    programming_language_python         int          not null,
    programming_language_r              int          not null,
    programming_language_sql            int          not null,
    programming_language_javascript     int          not null,
    programming_language_verilog        int          not null,
    programming_language_assembler      int          not null,
    programming_language_assembler_type varchar(255) null,
    computer_systems_type               varchar(255) null,
    ta_courses_preference               varchar(255) null,
    ta_courses_preference_hidden        varchar(255) null,
    ta_courses_not_preference           varchar(255) null,
    ta_courses_not_preference_hidden    varchar(255) null,
    previous_teaching_exp1title         varchar(255) null,
    previous_teaching_exp1where         varchar(255) null,
    previous_teaching_exp1date          varchar(255) null,
    previous_teaching_exp2title         varchar(255) null,
    previous_teaching_exp2where         varchar(255) null,
    previous_teaching_exp2date          varchar(255) null,
    previous_teaching_exp3title         varchar(255) null,
    previous_teaching_exp3where         varchar(255) null,
    previous_teaching_exp3date          varchar(255) null,
    apply_headline                      varchar(255) null,
    apply_cover_letter                  varchar(255) null,
    tating                              double       not null,
    tating_count                        bigint       not null,
    recommend_rating                    double       not null,
    recommend_rating_count              bigint       not null,
    homepage                            varchar(255) null,
    avatar                              varchar(255) null,
    created_time                        varchar(255) null,
    is_active                           varchar(255) null,
    constraint fk_tajob_application_applicant_id
        foreign key (applicant_id) references user (id),
    constraint fk_tajob_application_tajob_id
        foreign key (tajob_id) references tajob (id)
);

create index ix_tajob_application_applicant_id
    on tajob_application (applicant_id);

create index ix_tajob_application_tajob_id
    on tajob_application (tajob_id);

create table taweekly_hours
(
    id            bigint auto_increment
        primary key,
    week          int                  not null,
    hours         int                  not null,
    approval      tinyint(1) default 0 not null,
    assignment_id bigint               null,
    constraint fk_taweekly_hours_assignment_id
        foreign key (assignment_id) references course_taassignment (id)
);

create index ix_taweekly_hours_assignment_id
    on taweekly_hours (assignment_id);

create table technology
(
    id                       bigint auto_increment
        primary key,
    technology_title         varchar(255) null,
    goals                    varchar(255) null,
    short_description        varchar(255) null,
    long_description         varchar(255) null,
    keywords                 varchar(255) null,
    p_iname                  varchar(255) null,
    team_members             varchar(255) null,
    fields                   varchar(255) null,
    organizations            varchar(255) null,
    pdf                      varchar(255) null,
    representative_paper_url varchar(255) null,
    rating                   double       not null,
    rating_count             bigint       not null,
    recommend_rating         double       not null,
    recommend_rating_count   bigint       not null,
    homepage                 varchar(255) null,
    registered_time          varchar(255) null,
    is_active                varchar(255) null,
    technology_publisher_id  bigint       null,
    constraint fk_technology_technology_publisher_id
        foreign key (technology_publisher_id) references user (id)
);

create index ix_technology_technology_publisher_id
    on technology (technology_publisher_id);

create table technology_usedin_project
(
    technology_id bigint not null,
    project_id    bigint not null,
    primary key (technology_id, project_id),
    constraint fk_technology_usedin_project_project
        foreign key (project_id) references project (id),
    constraint fk_technology_usedin_project_technology
        foreign key (technology_id) references technology (id)
);

create index ix_technology_usedin_project_project
    on technology_usedin_project (project_id);

create index ix_technology_usedin_project_technology
    on technology_usedin_project (technology_id);

create index ix_user_project_zone_id
    on user (project_zone_id);

create table user_organization
(
    organization_id bigint not null,
    user_id         bigint not null,
    primary key (organization_id, user_id),
    constraint fk_user_organization_organization
        foreign key (organization_id) references organization (id),
    constraint fk_user_organization_user
        foreign key (user_id) references user (id)
);

create index ix_user_organization_organization
    on user_organization (organization_id);

create index ix_user_organization_user
    on user_organization (user_id);

create table user_participation_project
(
    user_id    bigint not null,
    project_id bigint not null,
    primary key (user_id, project_id),
    constraint fk_user_participation_project_project
        foreign key (project_id) references project (id),
    constraint fk_user_participation_project_user
        foreign key (user_id) references user (id)
);

create index ix_user_participation_project_project
    on user_participation_project (project_id);

create index ix_user_participation_project_user
    on user_participation_project (user_id);

