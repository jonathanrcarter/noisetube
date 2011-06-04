class AddPrivacy < ActiveRecord::Migration
  
  def self.up
     add_column :users, :public, :boolean, :default => true
     rename_column :tracks, :sound_id, :session_id 
     rename_table :tracks, :measures
     rename_table :sounds, :session
  end

  def self.down
    rename_table :session, :sounds 
    rename_table :measures, :tracks
    rename_column :tracks, :session_id , :sound_id
    remove_column :users, :public
  end
  
end
