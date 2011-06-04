class Addsegment < ActiveRecord::Migration
  def self.up
    create_table :segment do |t|
      t.line_string :geom, :srid=>4326
      t.references :urban
    end
     add_index :segment, "geom", :spatial=>true
  end

  def self.down
  end
end
