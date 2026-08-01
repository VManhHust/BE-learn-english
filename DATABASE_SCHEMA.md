# Cau truc database sau Flyway

Tai lieu nay mo ta cac bang con ton tai sau khi chay day du migration Flyway trong project `BE-learn-english`.

## users

Muc dich: Luu thong tin tai khoan nguoi dung, dang nhap va trang thai PRO.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| id | BIGSERIAL | Khoa chinh cua user. |
| email | VARCHAR(255) | Email dang nhap, duy nhat. |
| password_hash | VARCHAR(60) | Mat khau da bam; co the null voi tai khoan OAuth. |
| display_name | VARCHAR(100) | Ten hien thi cua user. |
| role | VARCHAR(10) | Vai tro, mac dinh `USER`. |
| status | VARCHAR(20) | Trang thai tai khoan: `ACTIVE`, `LOCK`, hoac `DELETE`. Chi `ACTIVE` duoc dang nhap va dung API. |
| google_id | VARCHAR(255) | ID Google neu user dang nhap bang Google. |
| created_at | TIMESTAMPTZ | Thoi diem tao tai khoan. |
| pro_expires_at | TIMESTAMPTZ | Thoi diem het han PRO. |
| pro_starts_at | TIMESTAMPTZ | Thoi diem bat dau kich hoat PRO. |

## refresh_tokens

Muc dich: Luu refresh token da bam de duy tri phien dang nhap.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| id | BIGSERIAL | Khoa chinh cua refresh token. |
| user_id | BIGINT | User so huu token; xoa user thi xoa token. |
| token_hash | VARCHAR(64) | Hash cua refresh token, duy nhat. |
| expires_at | TIMESTAMPTZ | Thoi diem token het han. |
| revoked | BOOLEAN | Danh dau token da bi thu hoi hay chua. |
| created_at | TIMESTAMPTZ | Thoi diem tao token. |

## learning_topic

Muc dich: Nhom chu de hoc tap cho cac bai hoc/bai tap, vi du video YouTube.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| id | BIGSERIAL | Khoa chinh cua chu de hoc tap. |
| topic_name | VARCHAR(255) | Ten chu de. |
| description | TEXT | Mo ta chu de. |
| type | VARCHAR(50) | Loai chu de, vi du `YOUTUBE`. |
| status | VARCHAR(30) | Trang thai xuat ban: `DRAFT`, `PUBLISHED`, hoac `ARCHIVED`. Public API chi hien thi `PUBLISHED`. |
| created_at | TIMESTAMPTZ | Thoi diem tao chu de. |

## youtube_channels

Muc dich: Luu metadata kenh YouTube dung cho bai hoc video.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| id | BIGSERIAL | Khoa chinh cua kenh. |
| channel_youtube_id | VARCHAR(255) | ID kenh tren YouTube, duy nhat. |
| channel_name | VARCHAR(255) | Ten kenh. |
| channel_img_url | VARCHAR(500) | Anh dai dien kenh. |
| channel_description | TEXT | Mo ta kenh. |
| channel_subscriber_count | BIGINT | So luong subscriber. |
| created_at | TIMESTAMPTZ | Thoi diem tao ban ghi. |

## learning_exercise

Muc dich: Luu bai tap hoc tap chinh, gom thong tin chung va quan he voi chu de.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| id | BIGSERIAL | Khoa chinh cua bai tap. |
| uuid | VARCHAR(255) | Ma dinh danh ngoai he thong, duy nhat. |
| type | VARCHAR(50) | Loai bai tap. |
| title | VARCHAR(500) | Tieu de bai tap. |
| module_count | INT | So module/cau trong bai tap. |
| vocabulary_level | VARCHAR(10) | Trinh do tu vung, vi du A1/A2/B1. |
| topic_id | BIGINT | Chu de hoc tap cua bai tap. |
| status | VARCHAR(30) | Trang thai xuat ban: `DRAFT`, `PUBLISHED`, hoac `ARCHIVED`. Public API chi hien thi `PUBLISHED`. |
| created_at | TIMESTAMPTZ | Thoi diem tao bai tap. |
| is_premium | BOOLEAN | Bai tap co yeu cau PRO hay khong. |

## learning_exercise_youtube_extension

Muc dich: Luu thong tin mo rong rieng cho bai tap dang video YouTube.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| id | BIGSERIAL | Khoa chinh cua extension. |
| video_id | VARCHAR(50) | ID video YouTube. |
| thumbnail_url | VARCHAR(500) | Anh thumbnail cua video. |
| duration_seconds | INT | Thoi luong video tinh bang giay. |
| learning_exercise_id | BIGINT | Bai tap cha; moi bai tap co toi da mot YouTube extension. |
| youtube_channel_id | BIGINT | Kenh YouTube cua video; co the null neu kenh bi xoa. |

## exercise_module_youtube_extension

Muc dich: Luu noi dung mo rong cho tung module/cau cua bai tap YouTube.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| id | BIGSERIAL | Khoa chinh cua module extension. |
| time_start_ms | INT | Thoi diem bat dau cau trong video, tinh bang millisecond. |
| time_end_ms | INT | Thoi diem ket thuc cau trong video, tinh bang millisecond. |
| correct_answer | TEXT | Cau tra loi/transcript dung bang tieng Anh. |
| vietnamese_text | TEXT | Ban dich/noi dung tieng Viet cua cau. |

## exercise_module

Muc dich: Luu cac module nho thuoc mot bai tap, vi du tung cau dictation/shadowing.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| id | BIGSERIAL | Khoa chinh cua module. |
| type | VARCHAR(50) | Loai module. |
| exercise_id | BIGINT | Bai tap cha. |
| youtube_module_extension_id | BIGINT | Extension YouTube cua module; duy nhat neu co. |

## learning_progress

Muc dich: Luu tien do hoc dictation cua user theo lesson.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| id | BIGSERIAL | Khoa chinh cua tien do. |
| user_id | BIGINT | User co tien do hoc. |
| lesson_id | BIGINT | ID lesson/bai hoc lien quan; khong co FK trong schema hien tai. |
| segment_results | JSONB | Ket qua tung segment: checked, skipped, accuracy, isGood. |
| user_inputs | JSONB | Input cua user theo tung segment. |
| completion_percentage | INTEGER | Phan tram hoan thanh tu 0 den 100. |
| is_completed | BOOLEAN | Da hoan thanh bai hoc hay chua. |
| completed_at | TIMESTAMP | Thoi diem hoan thanh. |
| updated_at | TIMESTAMP | Thoi diem cap nhat gan nhat. |
| created_at | TIMESTAMP | Thoi diem tao tien do. |

## vocabulary_bank

Muc dich: Luu cac tu user da luu vao kho tu vung ca nhan.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| id | BIGSERIAL | Khoa chinh cua tu da luu. |
| user_id | BIGINT | User so huu tu da luu. |
| word | VARCHAR(100) | Tu/cum tu duoc luu. |
| added_at | TIMESTAMP | Thoi diem user luu tu. |

## video_notes

Muc dich: Luu ghi chu cua user khi xem/luyen video exercise.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| id | BIGSERIAL | Khoa chinh cua ghi chu. |
| user_id | BIGINT | User tao ghi chu. |
| video_id | BIGINT | Tham chieu toi `learning_topic`; ten truong legacy dang goi la video. |
| exercise_module_id | BIGINT | Module/cau YouTube ma ghi chu gan vao. |
| note_content | TEXT | Noi dung ghi chu. |
| created_at | TIMESTAMP | Thoi diem tao ghi chu. |

## email_verifications

Muc dich: Luu ma xac minh email cho cac luong dang ky/quen mat khau.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| id | BIGSERIAL | Khoa chinh cua ma xac minh. |
| email | VARCHAR(255) | Email can xac minh. |
| code_hash | VARCHAR(64) | Hash cua ma xac minh. |
| expires_at | TIMESTAMPTZ | Thoi diem ma het han. |
| used | BOOLEAN | Ma da duoc su dung hay chua. |
| created_at | TIMESTAMPTZ | Thoi diem tao ma. |

## user_streak_checkins

Muc dich: Luu moi ngay user co check-in streak hay khong.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| id | BIGSERIAL | Khoa chinh cua check-in. |
| user_id | BIGINT | User check-in. |
| check_in_date | DATE | Ngay check-in. |
| created_at | TIMESTAMPTZ | Thoi diem tao check-in. |

## vocabulary_deck

Muc dich: Luu bo tu vung, la cap cao nhat trong module vocabulary flashcard.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| id | BIGSERIAL | Khoa chinh cua bo tu. |
| slug | VARCHAR(120) | Slug duy nhat de dinh danh bo tu. |
| title | VARCHAR(255) | Ten bo tu. |
| category | VARCHAR(120) | Nhom danh muc cua bo tu. |
| description | TEXT | Mo ta bo tu. |
| cover_color | VARCHAR(30) | Mau dai dien cua bo tu. |
| image_url | TEXT | URL anh cover cua bo tu. |
| status | VARCHAR(30) | Trang thai hien thi, mac dinh `PUBLISHED`. |
| is_premium | BOOLEAN | Bo tu co yeu cau PRO hay khong. |
| learner_count | INTEGER | So luong nguoi hoc hien thi/thong ke. |
| sort_order | INTEGER | Thu tu sap xep bo tu. |
| created_at | TIMESTAMPTZ | Thoi diem tao bo tu. |
| updated_at | TIMESTAMPTZ | Thoi diem cap nhat bo tu. |

## vocabulary_topic

Muc dich: Luu chu de nho ben trong mot bo tu vung.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| id | BIGSERIAL | Khoa chinh cua chu de tu vung. |
| deck_id | BIGINT | Bo tu cha. |
| slug | VARCHAR(120) | Slug cua chu de, duy nhat trong tung deck. |
| title | VARCHAR(255) | Ten chu de. |
| description | TEXT | Mo ta chu de. |
| thumbnail_url | TEXT | Anh thumbnail cua chu de. |
| status | VARCHAR(30) | Trang thai xuat ban: `DRAFT`, `PUBLISHED`, hoac `ARCHIVED`. Public API chi hien thi `PUBLISHED`. |
| sort_order | INTEGER | Thu tu sap xep chu de trong deck. |
| created_at | TIMESTAMPTZ | Thoi diem tao chu de. |
| updated_at | TIMESTAMPTZ | Thoi diem cap nhat chu de. |

## vocabulary_word

Muc dich: Luu tung the tu vung de hoc va on tap.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| id | BIGSERIAL | Khoa chinh cua tu vung. |
| topic_id | BIGINT | Chu de tu vung cha. |
| word | VARCHAR(160) | Tu/cum tu tieng Anh; dang duoc unique toan bang. |
| part_of_speech | VARCHAR(80) | Tu loai, vi du noun/verb/adjective. |
| ipa_us | VARCHAR(120) | Phien am giong My. |
| ipa_uk | VARCHAR(120) | Phien am giong Anh. |
| audio_us_url | TEXT | URL audio phat am giong My. |
| audio_uk_url | TEXT | URL audio phat am giong Anh. |
| english_definition | TEXT | Dinh nghia bang tieng Anh. |
| vietnamese_definition | TEXT | Giai thich bang tieng Viet. |
| vietnamese_translation | VARCHAR(255) | Nghia ngan gon bang tieng Viet. |
| example_sentence | TEXT | Cau vi du tieng Anh. |
| example_sentence_vi | TEXT | Ban dich cau vi du tieng Viet. |
| image_url | TEXT | Anh minh hoa cua tu. |
| status | VARCHAR(30) | Trang thai xuat ban: `DRAFT`, `PUBLISHED`, hoac `ARCHIVED`. Public API chi hien thi `PUBLISHED`. |
| sort_order | INTEGER | Thu tu hien thi trong topic. |
| created_at | TIMESTAMPTZ | Thoi diem tao tu. |
| updated_at | TIMESTAMPTZ | Thoi diem cap nhat tu. |

## user_vocabulary_topic_progress

Muc dich: Luu tien do hoc cua user theo tung chu de tu vung.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| id | BIGSERIAL | Khoa chinh cua tien do topic. |
| user_id | BIGINT | User dang hoc. |
| topic_id | BIGINT | Topic tu vung dang theo doi. |
| learned_words | INTEGER | So tu da hoc trong topic. |
| current_word_index | INTEGER | Vi tri tu hien tai/tiep theo trong topic. |
| completion_percentage | INTEGER | Phan tram hoan thanh topic. |
| is_completed | BOOLEAN | Topic da hoc xong hay chua. |
| completed_at | TIMESTAMPTZ | Thoi diem hoan thanh topic. |
| created_at | TIMESTAMPTZ | Thoi diem tao tien do. |
| updated_at | TIMESTAMPTZ | Thoi diem cap nhat tien do. |
| shuffle_seed | BIGINT | Seed dung de tron thu tu tu con lai khi user chon shuffle. |

## user_vocabulary_word_progress

Muc dich: Luu lich su danh gia va lich on tap cua user theo tung tu vung.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| id | BIGSERIAL | Khoa chinh cua tien do tu. |
| user_id | BIGINT | User dang hoc/on tu. |
| word_id | BIGINT | Tu vung duoc theo doi. |
| status | VARCHAR(30) | Trang thai hien tai, chi nhan `MASTERED` hoac `NOT_MASTERED`. |
| last_rating | VARCHAR(30) | Lan danh gia gan nhat, `MASTERED` hoac `NOT_MASTERED`. |
| review_count | INTEGER | Tong so lan user da danh gia/on tap tu nay. |
| correct_count | INTEGER | So lan user bam `MASTERED`. |
| not_mastered_count | INTEGER | So lan user bam `NOT_MASTERED`; dung de uu tien tu kho. |
| ease_factor | NUMERIC(4,2) | He so do de/nho cua tu trong lich on tap. |
| next_review_at | TIMESTAMPTZ | Thoi diem can on tap tiep theo. |
| mastered_review_stage | INTEGER | Bac on tap hien tai cua tu da mastered. |
| review_completed | BOOLEAN | Da hoan thanh chu ky on tap mastered hay chua. |
| learned_at | TIMESTAMPTZ | Thoi diem user lan dau bam mastered cho tu. |
| created_at | TIMESTAMPTZ | Thoi diem tao tien do tu. |
| updated_at | TIMESTAMPTZ | Thoi diem cap nhat tien do tu. |

## pro_plan_configs

Muc dich: Cau hinh cac goi PRO, gia, thoi han va quyen loi hien thi/quan tri trong CMS.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| id | BIGSERIAL | Khoa chinh cua goi PRO. |
| code | VARCHAR(20) | Ma goi duy nhat, duoc luu vao `payment_orders.plan_code`. |
| name | VARCHAR(120) | Ten goi hien thi. |
| description | TEXT | Mo ta goi. |
| amount | BIGINT | Gia goi tinh bang VND; phai lon hon 0. |
| duration_days | INTEGER | So ngay kich hoat PRO; de trong neu la goi tron doi. |
| benefits | TEXT | Danh sach quyen loi PRO co ban. |
| special_benefits | TEXT | Quyen loi dac biet/noi bat cua goi. |
| status | VARCHAR(20) | Trang thai goi, `ACTIVE` hoac `INACTIVE`. |
| featured | BOOLEAN | Goi co duoc danh dau noi bat hay khong. |
| sort_order | INTEGER | Thu tu hien thi va xep hang nang cap. |
| created_at | TIMESTAMPTZ | Thoi diem tao goi. |
| updated_at | TIMESTAMPTZ | Thoi diem cap nhat goi. |

## payment_orders

Muc dich: Luu don thanh toan mua PRO va thoi han kich hoat tu don do.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| id | UUID | Khoa chinh cua don thanh toan. |
| user_id | BIGINT | User tao don. |
| payment_code | VARCHAR(20) | Ma thanh toan duy nhat. |
| amount | BIGINT | So tien thanh toan; phai lon hon 0. |
| status | VARCHAR(20) | Trang thai don, vi du pending/paid/expired tuy logic app. |
| expires_at | TIMESTAMPTZ | Thoi diem don thanh toan het han. |
| paid_at | TIMESTAMPTZ | Thoi diem thanh toan thanh cong. |
| sepay_transaction_id | BIGINT | ID giao dich tu Sepay, duy nhat neu co. |
| bank_reference_code | VARCHAR(255) | Ma tham chieu ngan hang. |
| created_at | TIMESTAMPTZ | Thoi diem tao don. |
| updated_at | TIMESTAMPTZ | Thoi diem cap nhat don. |
| plan_code | VARCHAR(20) | Goi PRO duoc mua, mac dinh du lieu cu la `YEARLY`. |
| pro_starts_at | TIMESTAMPTZ | Thoi diem bat dau PRO tu don nay. |
| pro_expires_at | TIMESTAMPTZ | Thoi diem het han PRO tu don nay. |

## payment_webhook_events

Muc dich: Luu webhook thanh toan da nhan de chong xu ly trung va debug payload.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| transaction_id | BIGINT | ID giao dich webhook, dong thoi la khoa chinh. |
| raw_payload | TEXT | Payload webhook nguyen ban. |
| received_at | TIMESTAMPTZ | Thoi diem he thong nhan webhook. |

## audit_logs

Muc dich: Luu lich su thao tac quan tri trong CMS de truy vet ai da tao/sua/xoa du lieu nao.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| id | BIGSERIAL | Khoa chinh cua audit log. |
| actor_user_id | BIGINT | ID admin thuc hien thao tac. |
| actor_email | VARCHAR(255) | Email admin thuc hien thao tac. |
| action | VARCHAR(20) | Hanh dong, vi du `CREATE`, `UPDATE`, `DELETE`. |
| resource | VARCHAR(120) | Nhom tai nguyen bi tac dong, vi du `lessons`, `vocabulary/words`. |
| resource_id | VARCHAR(120) | ID ban ghi bi tac dong neu suy ra duoc tu URL. |
| http_method | VARCHAR(10) | HTTP method cua request. |
| request_path | VARCHAR(500) | Duong dan API duoc goi. |
| query_string | TEXT | Query string cua request neu co. |
| response_status | INTEGER | HTTP status tra ve. |
| success | BOOLEAN | Request thanh cong hay that bai. |
| ip_address | VARCHAR(80) | IP client, uu tien header proxy neu co. |
| user_agent | TEXT | User-Agent cua trinh duyet/client. |
| details | JSONB | Thong tin bo sung, vi du query va loi neu request fail. |
| created_at | TIMESTAMPTZ | Thoi diem ghi log. |

## user_notifications

Muc dich: Luu trung tam thong bao hoc tap trong app, trang thai da doc va du lieu dieu huong cho tung user.

| Truong | Kieu du lieu | Y nghia |
| --- | --- | --- |
| id | BIGSERIAL | Khoa chinh cua thong bao. |
| user_id | BIGINT | User nhan thong bao. |
| type | VARCHAR(50) | Loai thong bao: nhac hoc, nhac streak, hoc tiep hoac noi dung moi duoc CMS xuat ban. |
| priority | VARCHAR(20) | Muc uu tien `NORMAL` hoac `HIGH`. |
| data | JSONB | Du lieu trung lap ngon ngu de frontend hien thi Viet/Anh. |
| action_url | TEXT | Duong dan den noi dung khi user bam thong bao. |
| dedupe_key | VARCHAR(200) | Khoa duy nhat theo user de chong tao trung trong cung chu ky. |
| read_at | TIMESTAMPTZ | Thoi diem user danh dau da doc. |
| expires_at | TIMESTAMPTZ | Thoi diem thong bao khong con tinh vao so chua doc. |
| created_at | TIMESTAMPTZ | Thoi diem tao thong bao. |
| updated_at | TIMESTAMPTZ | Thoi diem dong bo du lieu thong bao gan nhat. |
