class Fixsession < ActiveRecord::Migration
  def self.up
    rename_table :session, :tracks
  end

  def self.down
    rename_table :tracks, :session
  end
end
