CREATE DATABASE skillsync_auth;
CREATE DATABASE skillsync_user;
CREATE DATABASE skillsync_mentor;
CREATE DATABASE skillsync_session;
CREATE DATABASE skillsync_notification;
CREATE DATABASE skillsync_review;
CREATE DATABASE skillsync_group;

GRANT ALL PRIVILEGES ON DATABASE skillsync_auth TO postgres;
GRANT ALL PRIVILEGES ON DATABASE skillsync_user TO postgres;
GRANT ALL PRIVILEGES ON DATABASE skillsync_mentor TO postgres;
GRANT ALL PRIVILEGES ON DATABASE skillsync_session TO postgres;
GRANT ALL PRIVILEGES ON DATABASE skillsync_notification TO postgres;
GRANT ALL PRIVILEGES ON DATABASE skillsync_review TO postgres;
GRANT ALL PRIVILEGES ON DATABASE skillsync_group TO postgres;
