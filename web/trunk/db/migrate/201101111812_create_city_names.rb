class CreateCityNames < ActiveRecord::Migration
  def self.up
    create_table(:city_names, :id => false) do |t|
       t.column :city_id, :integer
       t.column :name, :integer
       t.column :language, :string
       t.timestamps
     end
  end

  def self.down
    drop_table :city_names
  end
end