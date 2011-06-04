class Addpatternpresence < ActiveRecord::Migration
  def self.up
    add_column :measures, :peak, :integer
    add_column :measures, :longexposure, :integer
  end

  def self.down
    
  end
end
