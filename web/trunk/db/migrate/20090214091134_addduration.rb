class Addduration < ActiveRecord::Migration
 
  def self.up
  
    add_column :tracks , :city, :string
    add_column :tracks , :public, :boolean
    remove_column :measures , :private
    rename_column :tracks , :lon, :lng
    
  end

  def self.down
    
  end
  
end
