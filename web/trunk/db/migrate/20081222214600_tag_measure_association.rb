class TagMeasureAssociation < ActiveRecord::Migration
  
  def self.up
    drop_table :sound_tags
    create_table :measure_tags do |t|
      t.references :measure
      t.references :tag           
    end
    remove_column :measures , :tag
  end

  def self.down
    
  end
end
