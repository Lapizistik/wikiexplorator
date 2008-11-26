#!/usr/bin/ruby -w
# :title: MediawikiDB Fields
# = Mediawiki Common Database Definitions

module Mediawiki
  class DB
    FIELDS_USER = %w{user_id user_name user_real_name user_email 
                      user_options user_touched user_email_authenticated 
                      user_email_token_expires user_registration 
                      user_newpass_time user_editcount}
    FIELDS_USER_GROUPS = %w{ug_user ug_group}
    FIELDS_TEXT = %w{old_id old_text old_flags}
    FIELDS_PAGE = %w{page_id page_namespace page_title 
                      page_restrictions 
                      page_counter page_is_redirect page_is_new 
                      page_random page_touched page_latest page_len}
    FIELDS_REVISION = %w{rev_id rev_page rev_text_id rev_comment 
                          rev_user rev_user_text rev_timestamp 
                          rev_minor_edit rev_deleted}
    FIELDS_GENRES = %w{page_id genres}
    FIELDS_ROLES = %w{user_id roles}
  end
end
