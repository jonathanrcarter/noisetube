class Changetrack < ActiveRecord::Migration
  def self.up
    remove_column :tracks, :name
    change_column :tracks, :start,:datetime, :null =>true
    change_column :tracks, :end,:datetime, :null =>true
    remove_column :tracks, :feeling
  end

  def self.down
    add_column :tracks, :name
    add_column :tracks, :feeling
    change_column :tracks, :start,:datetime, :null =>false
    change_column :tracks, :end,:datetime, :null =>false
    
  end
end
