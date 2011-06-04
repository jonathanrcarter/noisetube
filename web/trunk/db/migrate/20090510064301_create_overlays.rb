class CreateOverlays < ActiveRecord::Migration
  def self.up
    create_table :overlays do |t|
      t.integer :track_id
      t.integer :loudness_index
      t.string :url
      t.polygon  :geom
      t.timestamps
    end
    add_index :overlays, :geom, :spatial=>true
    add_column :cities, :timezone, :string
  end

  def self.down
    drop_table :overlays
  end
end
