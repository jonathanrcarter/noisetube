class CreateUrbanelements < ActiveRecord::Migration
  def self.up
    create_table :urbanelements do |t|
      t.string :name
      t.integer :type
      t.integer :city_id
      t.timestamps
    end
  end

  def self.down
    drop_table :urbanelements
  end
end
