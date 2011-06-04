class AddRegionTable < ActiveRecord::Migration
  def self.up
    add_column(:cities, :region_id, :integer)
    remove_column(:cities, :region)
    create_table(:regions, :id => true) do |t|
       t.column :name, :string
       t.column :country, :string
       t.timestamps
     end
  end

  def self.down
    add_column(:cities, :region, :string)
    remove_column(:cities, :region_id)
    drop_table :regions
  end
end
